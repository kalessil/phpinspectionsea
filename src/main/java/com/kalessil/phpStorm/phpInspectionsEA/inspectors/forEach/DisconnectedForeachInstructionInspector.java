package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DisconnectedForeachInstructionInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_USING_CLONE = false;

    private static final String messageDisconnected = "This statement seems to be disconnected from its parent foreach.";
    private static final String messageUseClone     = "Objects should be created outside of a loop and cloned instead.";

    @NotNull
    public String getShortName() {
        return "DisconnectedForeachInstructionInspection";
    }

    private enum ExpressionType {
        INCREMENT, DECREMENT,
        CLONE, NEW, DOM_ELEMENT_CREATE,
        CONTROL_STATEMENTS, ASSIGNMENT,
        ACCUMULATE_IN_ARRAY, OTHER
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                final List<Variable> variables   = foreach.getVariables();
                final GroupStatement foreachBody = ExpressionSemanticUtil.getGroupStatement(foreach);
                /* ensure foreach structure is ready for inspection */
                if (null != foreachBody && variables.size() > 0) {
                    /* pre-collect introduced and internally used variables */
                    final Set<String> allModifiedVariables
                            = variables.stream().map(PhpNamedElement::getName).collect(Collectors.toSet());

                    final Map<PsiElement, Set<String>> instructionDependencies = new HashMap<>();
                    /* iteration 1 - investigate what are dependencies and influence */
                    for (final PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            final Set<String> individualDependencies = new HashSet<>();

                            instructionDependencies.put(oneInstruction, individualDependencies);
                            investigateInfluence((PhpPsiElement) oneInstruction, individualDependencies, allModifiedVariables);
                        }
                    }

                    /* iteration 2 - analyse dependencies */
                    for (final PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            boolean isDependOnModifiedVariables = false;

                            /* check if any dependency is overridden */
                            final Set<String> individualDependencies = instructionDependencies.get(oneInstruction);
                            if (null != individualDependencies && individualDependencies.size() > 0) {
                                /* contains not only this */
                                for (final String dependencyName : individualDependencies) {
                                    if (allModifiedVariables.contains(dependencyName)) {
                                        isDependOnModifiedVariables = true;
                                        break;
                                    }
                                }
                            }

                            /* verify and report if violation detected */
                            if (!isDependOnModifiedVariables) {
                                final ExpressionType target = getExpressionType(oneInstruction);
                                if (
                                    ExpressionType.NEW                 != target &&
                                    ExpressionType.ASSIGNMENT          != target &&
                                    ExpressionType.CLONE               != target &&
                                    ExpressionType.INCREMENT           != target &&
                                    ExpressionType.DECREMENT           != target &&
                                    ExpressionType.DOM_ELEMENT_CREATE  != target &&
                                    ExpressionType.ACCUMULATE_IN_ARRAY != target &&
                                    ExpressionType.CONTROL_STATEMENTS  != target
                                ) {
                                    /* loops, ifs, switches, try's needs to be reported on keyword, others - complete */
                                    final PsiElement reportingTarget =
                                            (
                                                oneInstruction instanceof ControlStatement ||
                                                oneInstruction instanceof Try ||
                                                oneInstruction instanceof PhpSwitch
                                            )
                                                    ? oneInstruction.getFirstChild()
                                                    : oneInstruction;

                                    /* secure exceptions with '<?= ?>' constructions, false-positives with html */
                                    if (!OpenapiTypesUtil.isPhpExpressionImpl(oneInstruction) && oneInstruction.getTextLength() > 0) {
                                        /* inner looping termination/continuation should be taken into account */
                                        final PsiElement loopInterrupter
                                            = PsiTreeUtil.findChildOfAnyType(oneInstruction, true, PhpBreak.class, PhpContinue.class, PhpThrow.class, PhpReturn.class);
                                        /* operating with variables should be taken into account */
                                        final boolean isVariablesUsed
                                            = null != PsiTreeUtil.findChildOfAnyType(oneInstruction, true, Variable.class);
                                        if (null == loopInterrupter && isVariablesUsed) {
                                            holder.registerProblem(reportingTarget, messageDisconnected);
                                        }
                                    }
                                }

                                if (SUGGEST_USING_CLONE && (ExpressionType.DOM_ELEMENT_CREATE == target || ExpressionType.NEW == target)) {
                                    holder.registerProblem(oneInstruction, messageUseClone);
                                }
                            }

                            /* cleanup dependencies details */
                            if (null != individualDependencies) {
                                individualDependencies.clear();
                            }
                        }
                    }
                    /* empty dependencies details container */
                    instructionDependencies.clear();
                }
            }

            private void investigateInfluence(
                @Nullable PhpPsiElement oneInstruction,
                @NotNull Set<String> individualDependencies,
                @NotNull Set<String> allModifiedVariables
            ) {
                for (final PsiElement variable : PsiTreeUtil.findChildrenOfType(oneInstruction, Variable.class)) {
                    final String variableName = ((Variable) variable).getName();
                    PsiElement valueContainer = variable;
                    PsiElement parent         = variable.getParent();
                    while (parent instanceof FieldReference) {
                        valueContainer = parent;
                        parent         = parent.getParent();
                    }
                    final PsiElement grandParent = parent.getParent();

                    /* writing into variable */
                    if (parent instanceof AssignmentExpression) {
                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                        if (assignment.getVariable() == valueContainer) {
                            /* we are modifying the variable */
                            allModifiedVariables.add(variableName);
                            /* self-assignment and field assignment makes the variable dependent on itself  */
                            if (
                                parent instanceof SelfAssignmentExpression ||
                                valueContainer instanceof FieldReference
                            ) {
                                individualDependencies.add(variableName);
                            }

                            continue;
                        }
                    }

                    /* adding into an arrays; we both depend and modify the container */
                    if (parent instanceof ArrayAccessExpression && valueContainer == ((ArrayAccessExpression) parent).getValue()) {
                        allModifiedVariables.add(variableName);
                        individualDependencies.add(variableName);
                    }

                    /* php-specific `list(...) =` , `[...] =` construction */
                    if (parent instanceof MultiassignmentExpression) {
                        final List<PhpPsiElement> variables = ((MultiassignmentExpression) parent).getVariables();
                        if (!variables.isEmpty()) {
                            if (variables.contains(variable)) {
                                allModifiedVariables.add(variableName);

                                variables.clear();
                                continue;
                            }
                            variables.clear();
                        }
                    }

                    /* php-specific variables introduction: preg_match[_all] exporting results into 3rd argument */
                    if (parent instanceof ParameterList && OpenapiTypesUtil.isFunctionReference(grandParent)) {
                        final FunctionReference call  = (FunctionReference) grandParent;
                        final String functionName     = call.getName();
                        final PsiElement[] parameters = call.getParameters();

                        // TODO: array_pop, array_shift, next, current, fwrite... -> use mapping function => argument modified
                        if (
                            3 == parameters.length && parameters[2] == variable &&
                            functionName != null && functionName.startsWith("preg_match")
                        ) {
                            allModifiedVariables.add(variableName);
                            continue;
                        }
                    }

                    /* an object consumes the variable, perhaps modification takes place */
                    if (parent instanceof ParameterList && grandParent instanceof MethodReference) {
                        final MethodReference reference    = (MethodReference) grandParent;
                        final PsiElement referenceOperator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                        if (OpenapiTypesUtil.is(referenceOperator, PhpTokenTypes.ARROW)) {
                            final PsiElement variableCandidate = reference.getFirstPsiChild();
                            if (variableCandidate instanceof Variable) {
                                allModifiedVariables.add(((Variable) variableCandidate).getName());
                                continue;
                            }
                        }
                    }

                    /* increment/decrement are also write operations */
                    final ExpressionType type = this.getExpressionType(parent);
                    if (ExpressionType.INCREMENT == type || ExpressionType.DECREMENT == type) {
                        allModifiedVariables.add(variableName);
                        individualDependencies.add(variableName);
                        continue;
                    }
                    /* TODO: lookup for array access and property access */

                    individualDependencies.add(variableName);
                }
            }

            @NotNull
            private ExpressionType getExpressionType(@Nullable PsiElement expression) {
                if (expression instanceof PhpBreak || expression instanceof PhpContinue || expression instanceof PhpReturn) {
                    return ExpressionType.CONTROL_STATEMENTS;
                }

                /* regular '...;' statements */
                if (OpenapiTypesUtil.isStatementImpl(expression)) {
                    return getExpressionType(((Statement) expression).getFirstPsiChild());
                }

                /* unary operations */
                if (expression instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) expression).getOperation();
                    IElementType operationType = null;
                    if (null != operation) {
                        operationType = operation.getNode().getElementType();
                    }
                    if (PhpTokenTypes.opINCREMENT == operationType) {
                        return ExpressionType.INCREMENT;
                    }
                    if (PhpTokenTypes.opDECREMENT == operationType) {
                        return ExpressionType.DECREMENT;
                    }
                }

                /* different types of assignments */
                if (expression instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) expression;
                    final PsiElement variable             = assignment.getVariable();
                    if (variable instanceof Variable) {
                        final PsiElement value = assignment.getValue();
                        if (value instanceof NewExpression) {
                            return ExpressionType.NEW;
                        } else if (value instanceof UnaryExpression) {
                            if (OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.kwCLONE)) {
                                return ExpressionType.CLONE;
                            }
                        } else if (value instanceof MethodReference) {
                            final MethodReference call = (MethodReference) value;
                            final String methodName    = call.getName();
                            if (methodName != null && methodName.equals("createElement")) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(call);
                                if (
                                    resolved instanceof Method &&
                                    ((Method) resolved).getFQN().equals("\\DOMDocument.createElement")
                                ) {
                                    return ExpressionType.DOM_ELEMENT_CREATE;
                                }
                            }
                        }

                        /* allow all assignations afterwards */
                        return ExpressionType.ASSIGNMENT;
                    }

                    /* accumulating something in external container */
                    if (variable instanceof ArrayAccessExpression) {
                        final ArrayAccessExpression storage = (ArrayAccessExpression) variable;
                        if (null == storage.getIndex() || null == storage.getIndex().getValue()) {
                            return ExpressionType.ACCUMULATE_IN_ARRAY;
                        }
                    }
                }

                return ExpressionType.OTHER;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Suggest using clone", SUGGEST_USING_CLONE, (isSelected) -> SUGGEST_USING_CLONE = isSelected));
    }
}
