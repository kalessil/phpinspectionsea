package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaticLocalVariablesUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'static $%v% = [...]' can be used here";

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

                /* Filtering step 1: variables assigned with array without dynamic parts */
                final List<Variable> candidates = new ArrayList<>();
                for (AssignmentExpression expression : PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class)) {
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
                    for (PhpReference injection : PsiTreeUtil.findChildrenOfType(value, PhpReference.class)) {
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
                    if (!canBeStatic) {
                        continue;
                    }

                    /* store a variable, uniqueness is not checked here */
                    candidates.add((Variable) variable);
                }


                /* Filtering step 2: only unique variables from candidates which are not parameters */
                final List<Variable> filteredCandidates = new ArrayList<>();
                final Set<String> paramsNames           = new HashSet<>();
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
                candidates.clear();


                /* Analysis itself (sub-routine): variable is used in read context, no dispatching by reference */
                final PhpEntryPointInstruction start = method.getControlFlow().getEntryPoint();
                PhpAccessVariableInstruction[] uses;
                for (Variable variable : filteredCandidates) {
                    final String variableName = variable.getName();
                    uses = PhpControlFlowUtil.getFollowingVariableAccessInstructions(start, variableName, false);
                    if (uses.length < 2) { // definition + at least 1 usage expected
                        continue;
                    }

                    boolean isModified = false;
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
                            /* TODO: make this nice */
                            parent instanceof ArrayAccessExpression || parent instanceof ArrayCreationExpression || parent instanceof ArrayHashElement ||
                            (parent instanceof PhpPsiElementImpl &&
                                    (parent.getParent() instanceof ArrayCreationExpression || parent.getParent() instanceof ArrayHashElement)
                            )
                        ) {
                            valueToCompareWith = parent;
                            parent             = parent.getParent();
                        }

                        /* un-set is a modification */
                        if (parent instanceof PhpUnset) {
                            isModified = true;
                            break;
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
                                if (PhpTokenTypes.opBIT_AND == refCandidate.getNode().getElementType()) {
                                    isModified = true;
                                    break;
                                }
                                refCandidate = refCandidate.getNextSibling();
                            }
                            if (isModified) {
                                break;
                            }

                            /* case when we are writing into the variable */
                            if (valueToCompareWith != assignedValue) {
                                isModified = true;
                                break;
                            }
                            continue;
                        }

                        /* is used in a function call? */
                        if (parent instanceof ParameterList) {
                            PsiElement grandParent = parent.getParent();
                            Function callable      = null;

                            /* try resolving constructor */
                            PsiElement nevv = grandParent instanceof NewExpression ? grandParent : null;
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

                            if (null != callable) {
                                final Parameter[] params = callable.getParameters();

                                int usedParamIndex = 0;
                                for (PsiElement usedParam : ((ParameterList) parent).getParameters()) {
                                    /* variadic or extra parameters */
                                    if (usedParamIndex > params.length) {
                                        break;
                                    }

                                    /* when param is reference, verify if we passing the value in */
                                    if (
                                        params[usedParamIndex].isPassByRef() &&
                                        PsiEquivalenceUtil.areElementsEquivalent(usedParam, valueToCompareWith)
                                    ) {
                                        isModified = true;
                                    }

                                    ++usedParamIndex;
                                }
                                if (isModified) {
                                    break;
                                }
                            }

                            continue;
                        }

                        if (parent instanceof ForeachStatement) {
                            /* TODO: array modification via reference values */
                            continue;
                        }
                    }

                    if (!isModified) {
                        final String message = messagePattern.replace("%v%", variableName);
                        holder.registerProblem(variable, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
