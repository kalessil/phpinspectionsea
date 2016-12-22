package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StaticLocalVariablesUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "Variable can be static: property or 'static $%v% = [...]' (compile-time initialization).";

    @NotNull
    public String getShortName() {
        return "StaticLocalVariablesUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* common expectations regarding the method and class */
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz || clazz.isInterface() || method.isAbstract() || FileSystemUtil.isTestClass(clazz)) {
                    return;
                }
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == body || 0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                    return;
                }

                /* filtering step 1: variables assigned with array without dynamic parts */
                final List<Variable> candidates = this.findCandidateExpressions(body);

                /* filtering step 2: only unique variables from candidates which are not parameters */
                final List<Variable> filteredCandidates = this.filterDuplicatesAndParameters(candidates, method);
                candidates.clear();

                /* analysis itself: variable is used in read context, without modification opportunities */
                final PhpEntryPointInstruction start = method.getControlFlow().getEntryPoint();
                for (Variable variable : filteredCandidates) {
                    if (this.canBeModified(variable, start)) {
                        continue;
                    }

                    final String message = messagePattern.replace("%v%", variable.getName());
                    holder.registerProblem(variable, message, ProblemHighlightType.WEAK_WARNING);
                }
                filteredCandidates.clear();
            }

            private boolean canBeModified(@NotNull Variable variable, @NotNull PhpEntryPointInstruction start) {
                final PhpAccessVariableInstruction[] uses =
                        PhpControlFlowUtil.getFollowingVariableAccessInstructions(start, variable.getName(), false);
                /* definition + at least 1 usage required for invoking the analysis */
                if (uses.length < 2) {
                    return false;
                }

                for (PhpAccessVariableInstruction instruction : uses) {
                    /* do not process variables, which we already identified */
                    final PhpPsiElement expression = instruction.getAnchor();
                    if (expression == variable) {
                        continue;
                    }

                    /* get reasonable parent and container for modification checks */
                    PsiElement valueToCompareWith = expression;
                    PsiElement parent             = expression.getParent();
                    while (
                        parent instanceof ArrayAccessExpression ||
                        parent instanceof ArrayCreationExpression || parent instanceof ArrayHashElement ||
                        (parent instanceof PhpPsiElementImpl && (
                            parent.getParent() instanceof ArrayCreationExpression ||
                            parent.getParent() instanceof ArrayHashElement
                            )
                        )
                    ) {
                        valueToCompareWith = parent;
                        parent             = parent.getParent();
                    }

                    /* un-set is a modification */
                    if (parent instanceof PhpUnset) {
                        return true;
                    }

                    /* assignment can modify the variable */
                    if (parent instanceof AssignmentExpression) {
                        final AssignmentExpression assign    = (AssignmentExpression) parent;
                        final PhpPsiElement assignedValue    = assign.getValue();
                        final PhpPsiElement assignedVariable = assign.getVariable();
                        if (null == assignedVariable || null == assignedValue) {
                            continue;
                        }

                        /* assign by reference will be treated as modifiable */
                        PsiElement refCandidate = assignedVariable.getNextSibling();
                        while (refCandidate != assignedValue) {
                            final IElementType type = refCandidate.getNode().getElementType();
                            if (PhpTokenTypes.opASGN == type) {
                                final String operator = refCandidate.getText().replaceAll("\\s", "");
                                if (operator.equals("=&")) {
                                    return true;
                                }
                            }
                            refCandidate = refCandidate.getNextSibling();
                        }

                        /* case when we are writing into the variable */
                        if (valueToCompareWith != assignedValue) {
                            return true;
                        }
                        continue;
                    }

                    /* variable can be dispatched into method/function/constructor by reference */
                    if (parent instanceof ParameterList) {
                        PsiElement grandParent = parent.getParent();
                        Function callable      = null;

                        /* try resolving constructor */
                        PsiElement nevv           = grandParent instanceof NewExpression ? grandParent : null;
                        final ClassReference ref  = null == nevv ? null : ((NewExpression) nevv).getClassReference();
                        final PsiElement newClass = null == ref ? null : ref.resolve();
                        if (newClass instanceof PhpClass) {
                            callable = ((PhpClass) newClass).getConstructor();
                        }

                        /* resolve function/method reference */
                        PsiElement call           = grandParent instanceof FunctionReference ? grandParent : null;
                        final PsiElement function = null == call ? null : ((FunctionReference) call).resolve();
                        if (function instanceof Function) {
                            callable = (Function) function;
                        }

                        /* if not resolved, everything can happen incl. referenced parameter */
                        if (null == callable) {
                            return true;
                        }

                        /* when resolved, inspect parameters and check if the value dispatched by ref */
                        final Parameter[] params = callable.getParameters();
                        int usedParamIndex       = 0;
                        for (PsiElement usedParam : ((ParameterList) parent).getParameters()) {
                            /* variadic or extra parameters */
                            if (0 == params.length || usedParamIndex > params.length) {
                                break;
                            }

                            /* when param is reference, verify if we passing the value in */
                            if (
                                params[usedParamIndex].isPassByRef() &&
                                PsiEquivalenceUtil.areElementsEquivalent(usedParam, valueToCompareWith)
                            ) {
                                return true;
                            }

                            ++usedParamIndex;
                        }
                        // continue;
                    }
                }

                return false;
            }

            @NotNull
            private List<Variable> findCandidateExpressions(@NotNull GroupStatement body) {
                final List<Variable> candidates = new ArrayList<>();

                Collection<AssignmentExpression> found = PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class);
                for (AssignmentExpression expression : found) {
                    /* check if a variable has been assigned a non-empty array and not yet static */
                    final PhpPsiElement variable = expression.getVariable();
                    final PhpPsiElement value    = expression.getValue();
                    if (
                        !(variable instanceof Variable) || !(value instanceof ArrayCreationExpression) ||
                        null == value.getFirstPsiChild() ||                     // an empty array
                        expression.getParent() instanceof PhpStaticStatement || // already static
                        !OpenapiTypesUtil.isAssignment(expression)              // filter openapi classes
                    ) {
                        continue;
                    }

                    /* analyze injections, ensure that only static content used */
                    boolean canBeStatic = true;
                    Collection<PhpReference> injected = PsiTreeUtil.findChildrenOfType(value, PhpReference.class);
                    for (PhpReference injection : injected) {
                        if (
                            injection instanceof ConstantReference ||
                            injection instanceof ClassConstantReference ||
                            injection instanceof ArrayCreationExpression    // un-expected, but as it is
                        ) {
                            continue;
                        }

                        canBeStatic = false;
                        break;
                    }
                    injected.clear();
                    if (!canBeStatic) {
                        continue;
                    }

                    /* store a variable, uniqueness is not checked here */
                    candidates.add((Variable) variable);
                }
                found.clear();

                return candidates;
            }

            @NotNull
            private List<Variable> filterDuplicatesAndParameters(@NotNull List<Variable> candidates, @NotNull Method method) {
                final List<Variable> filteredCandidates = new ArrayList<>();

                final Set<String> paramsNames = new HashSet<>();
                for (Parameter param : method.getParameters()) {
                    paramsNames.add(param.getName());
                }
                final boolean hasParams = paramsNames.size() > 0;

                for (Variable variable : candidates) {
                    if (hasParams && paramsNames.contains(variable.getName())) {
                        continue;
                    }

                    boolean isDuplicated = false;
                    for (Variable possibleDuplicate : candidates) {
                        if (variable != possibleDuplicate && PsiEquivalenceUtil.areElementsEquivalent(variable, possibleDuplicate)) {
                            isDuplicated = true;
                            break;
                        }
                    }
                    if (isDuplicated) {
                        continue;
                    }

                    filteredCandidates.add(variable);
                }
                paramsNames.clear();

                return filteredCandidates;
            }
        };
    }
}
