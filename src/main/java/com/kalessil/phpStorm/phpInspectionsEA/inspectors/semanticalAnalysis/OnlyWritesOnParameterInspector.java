package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OnlyWritesOnParameterInspector extends BasePhpInspection {
    private static final String messageOnlyWrites = "Parameter/variable is overridden, but is never used or appears outside of the scope.";
    private static final String messageUnused     = "The variable seems to be not used.";

    @NotNull
    public String getShortName() {
        return "OnlyWritesOnParameterInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
               this.visitPhpFunction(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                Arrays.stream(function.getParameters())
                        .filter(parameter  -> !parameter.getName().isEmpty() && !parameter.isPassByRef())
                        .filter(parameter  -> {
                            final PhpType declaredType = parameter.getDeclaredType().filterUnknown().filterNull();
                            final boolean isObject     =
                                !declaredType.isEmpty() &&
                                declaredType.getTypes().stream().anyMatch(t -> {
                                    final String type = Types.getType(t);
                                    return type.equals(Types.strObject) || type.startsWith("\\");
                                });
                            return !isObject;
                        })
                        .forEach(parameter -> this.analyzeAndReturnUsagesCount(parameter.getName(), function));

                final List<Variable> variables = ExpressionSemanticUtil.getUseListVariables(function);
                if (variables != null) {
                    this.checkUseVariables(variables, function);
                    variables.clear();
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                final PsiElement variable = assignmentExpression.getVariable();
                if (variable instanceof Variable) {
                    /* false-positives: predefined and global variables */
                    final String variableName = ((Variable) variable).getName();
                    if (variableName.isEmpty() || ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                        return;
                    }
                    /* filter target contexts: we are supporting only certain of them */
                    final PsiElement parent = assignmentExpression.getParent();
                    final boolean isTargetContext =
                        parent instanceof ParenthesizedExpression ||
                        (parent instanceof BinaryExpression && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(((BinaryExpression) parent).getOperationType())) ||
                        parent instanceof ArrayIndex ||
                        OpenapiTypesUtil.isAssignment(parent);
                    if (isTargetContext) {
                        final PsiElement scope = ExpressionSemanticUtil.getScope(assignmentExpression);
                        if (scope != null) {
                            final Function function = (Function) scope;
                            if (Arrays.stream(function.getParameters()).noneMatch(parameter -> parameter.getName().equals(variableName))) {
                                final List<Variable> uses   = ExpressionSemanticUtil.getUseListVariables(function);
                                final boolean isUseVariable = uses != null && uses.stream().anyMatch(candidate -> candidate.getName().equals(variableName));
                                if (!isUseVariable) {
                                    this.analyzeAndReturnUsagesCount(variableName, function);
                                }
                            }
                        }
                    }
                }
            }

            private void checkUseVariables(@NotNull List<Variable> variables, @NotNull PhpScopeHolder scopeHolder) {
                for (final Variable variable : variables) {
                    final String parameterName = variable.getName();
                    if (!parameterName.isEmpty()) {
                        PsiElement previous = variable.getPrevSibling();
                        if (previous instanceof PsiWhiteSpace) {
                            previous = previous.getPrevSibling();
                        }

                        if (OpenapiTypesUtil.is(previous, PhpTokenTypes.opBIT_AND)) {
                            if (getVariableUsages(parameterName, scopeHolder).length == 0) {
                                holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                            }
                        } else if (this.analyzeAndReturnUsagesCount(parameterName, scopeHolder) == 0) {
                            holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }
                    }
                }
            }

            private int analyzeAndReturnUsagesCount(@NotNull String parameterName, @NotNull PhpScopeHolder scopeHolder) {
                final PhpAccessVariableInstruction[] usages = getVariableUsages(parameterName, scopeHolder);
                if (usages.length == 0) {
                    return usages.length;
                }

                final List<PsiElement> targetExpressions = new ArrayList<>();

                boolean isReference       = false;
                int intCountReadAccesses  = 0;
                int intCountWriteAccesses = 0;
                for (final PhpAccessVariableInstruction instruction : usages) {
                    final PsiElement variable = instruction.getAnchor();
                    final PsiElement parent   = variable.getParent();

                    if (parent instanceof ArrayAccessExpression) {
                        /* find out which expression is holder */
                        PsiElement objLastSemanticExpression = variable;
                        PsiElement objTopSemanticExpression  = objLastSemanticExpression.getParent();
                        /* TODO: iterator for array access expression */
                        while (objTopSemanticExpression instanceof ArrayAccessExpression) {
                            objLastSemanticExpression = objTopSemanticExpression;
                            objTopSemanticExpression  = objTopSemanticExpression.getParent();
                        }

                        /* estimate operation type */
                        if (
                            objTopSemanticExpression instanceof AssignmentExpression &&
                            ((AssignmentExpression) objTopSemanticExpression).getVariable() == objLastSemanticExpression
                        ) {
                            intCountWriteAccesses++;
                            if (isReference) {
                                /* when modifying the reference it's link READ and linked WRITE semantics */
                                intCountReadAccesses++;
                            } else {
                                /* when modifying non non-reference, register as write only access for reporting */
                                targetExpressions.add(objLastSemanticExpression);
                            }
                            continue;
                        }

                        if (objTopSemanticExpression instanceof UnaryExpression) {
                            final PsiElement operation  = ((UnaryExpression) objTopSemanticExpression).getOperation();
                            final IElementType operator = operation == null ? null : operation.getNode().getElementType();
                            if (operator == PhpTokenTypes.opINCREMENT || operator == PhpTokenTypes.opDECREMENT) {
                                targetExpressions.add(objLastSemanticExpression);

                                ++intCountWriteAccesses;
                                continue;
                            }
                        }
                        intCountReadAccesses++;
                        continue;
                    }

                    /* ++/-- operations */
                    if (parent instanceof UnaryExpression) {
                        final PsiElement operation  = ((UnaryExpression) parent).getOperation();
                        final IElementType operator = operation == null ? null : operation.getNode().getElementType();
                        if (operator == PhpTokenTypes.opINCREMENT || operator == PhpTokenTypes.opDECREMENT) {
                            ++intCountWriteAccesses;
                            if (isReference) {
                                /* when modifying the reference it's link READ and linked WRITE semantics */
                                ++intCountReadAccesses;
                            } else {
                                /* when modifying non-reference, register as write only access for reporting */
                                targetExpressions.add(parent);
                            }
                        }
                        if (!OpenapiTypesUtil.isStatementImpl(parent.getParent())) {
                            ++intCountReadAccesses;
                        }
                        continue;
                    }

                    if (parent instanceof SelfAssignmentExpression) {
                        final SelfAssignmentExpression selfAssignment = (SelfAssignmentExpression) parent;
                        final PsiElement sameVariableCandidate        = selfAssignment.getVariable();
                        if (sameVariableCandidate instanceof Variable) {
                            final Variable candidate = (Variable) sameVariableCandidate;
                            if (candidate.getName().equals(parameterName)) {
                                ++intCountWriteAccesses;
                                if (isReference) {
                                    /* when modifying the reference it's link READ and linked WRITE semantics */
                                    ++intCountReadAccesses;
                                } else {
                                    /* when modifying non-reference, register as write only access for reporting */
                                    targetExpressions.add(variable);
                                }
                                if (!OpenapiTypesUtil.isStatementImpl(parent.getParent())) {
                                    ++intCountReadAccesses;
                                }
                                continue;
                            }
                        }
                    }

                    /* if variable assigned with reference, we need to preserve this information for correct checks */
                    if (parent instanceof AssignmentExpression) {
                        /* ensure variable with the same name being written */
                        final AssignmentExpression referenceAssignmentCandidate = (AssignmentExpression) parent;
                        final PsiElement sameVariableCandidate                  = referenceAssignmentCandidate.getVariable();
                        if (sameVariableCandidate instanceof Variable) {
                            final Variable candidate = (Variable) sameVariableCandidate;
                            if (candidate.getName().equals(parameterName)) {
                                ++intCountWriteAccesses;
                                if (isReference) {
                                    /* when modifying the reference it's link READ and linked WRITE semantics */
                                    ++intCountReadAccesses;
                                }
                                /* now ensure operation is assignment of reference */
                                PsiElement operation = sameVariableCandidate.getNextSibling();
                                while (operation != null && !OpenapiTypesUtil.is(operation, PhpTokenTypes.opASGN)) {
                                    operation = operation.getNextSibling();
                                }
                                if (operation != null && operation.getText().replaceAll("\\s+", "").equals("=&")) {
                                    isReference = true;
                                }
                                /* false-negative: inline assignment result has been used */
                                if (usages.length == 2 && usages[0].getAnchor() == usages[1].getAnchor()) {
                                    holder.registerProblem(sameVariableCandidate, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                                    return 1;
                                }
                                continue;
                            }
                        }
                    }

                    /* local variables access wrongly reported write in some cases, so rely on custom checks */
                    if (
                        parent instanceof ParameterList ||
                        parent instanceof PhpUseList ||
                        parent instanceof PhpUnset ||
                        parent instanceof PhpEmpty ||
                        parent instanceof PhpIsset ||
                        parent instanceof ForeachStatement
                    ) {
                        intCountReadAccesses++;
                        continue;
                    }


                    /* ok variable usage works well with openapi */
                    final PhpAccessInstruction.Access instructionAccess = instruction.getAccess();
                    if (instructionAccess.isWrite()) {
                        targetExpressions.add(variable);
                        ++intCountWriteAccesses;
                    } else if (instructionAccess.isRead()) {
                        ++intCountReadAccesses;
                    }
                }


                if (intCountReadAccesses == 0 && intCountWriteAccesses > 0) {
                    for (final PsiElement targetExpression : targetExpressions) {
                        holder.registerProblem(targetExpression, messageOnlyWrites, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
                targetExpressions.clear();

                return usages.length;
            }
        };
    }

    @NotNull
    static private PhpAccessVariableInstruction[] getVariableUsages(@NotNull String parameterName, @NotNull PhpScopeHolder scopeHolder) {
        return PhpControlFlowUtil.getFollowingVariableAccessInstructions(
                scopeHolder.getControlFlow().getEntryPoint(),
                parameterName,
                false
        );
    }
}