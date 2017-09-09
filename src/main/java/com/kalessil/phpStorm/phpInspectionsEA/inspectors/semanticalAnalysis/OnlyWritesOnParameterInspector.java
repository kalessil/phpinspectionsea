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
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
            /* re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.checkParameters(method.getParameters(), method);
            }

            public void visitPhpFunction(Function function) {
                this.checkParameters(function.getParameters(), function);

                final List<Variable> variables = ExpressionSemanticUtil.getUseListVariables(function);
                if (null != variables) {
                    this.checkUseVariables(variables, function);
                }
            }

            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                final PsiElement objVariable = assignmentExpression.getVariable();
                /* check assignments containing variable as container */
                if (objVariable instanceof Variable) {
                    final String variableName = ((Variable) objVariable).getName();
                    if (StringUtils.isEmpty(variableName) || ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                        return;
                    }

                    /* expression is located in function/method */
                    final PsiElement parentScope = ExpressionSemanticUtil.getScope(assignmentExpression);
                    if (null != parentScope) {
                        /* ensure it's not parameter, as it checked anyway */
                        for (Parameter objParameter : ((Function) parentScope).getParameters()) {
                            final String parameterName = objParameter.getName();
                            if (StringUtils.isEmpty(parameterName)) {
                                continue;
                            }

                            /* skip assignment check - it writes to parameter */
                            if (parameterName.equals(variableName)) {
                                return;
                            }
                        }

                        /* ensure it's not use list parameter of closure */
                        final List<Variable> useList = ExpressionSemanticUtil.getUseListVariables((Function) parentScope);
                        if (null != useList) {
                            /* use-list is found */
                            for (Variable useVariable : useList) {
                                final String useVariableName = useVariable.getName();
                                if (StringUtils.isEmpty(useVariableName)) {
                                    continue;
                                }

                                /* skip assignment check - it writes to used variable */
                                if (useVariableName.equals(variableName)) {
                                    useList.clear();
                                    return;
                                }
                            }
                            useList.clear();
                        }

                        /* verify variable usage */
                        this.analyzeAndReturnUsagesCount(variableName, (PhpScopeHolder) parentScope);
                    }
                }
            }

            private void checkParameters(Parameter[] parameters, @NotNull PhpScopeHolder scopeHolder) {
                for (Parameter parameter : parameters) {
                    if (parameter.isPassByRef()) {
                        continue;
                    }

                    final String parameterName = parameter.getName();
                    if (StringUtils.isEmpty(parameterName)) {
                        continue;
                    }

                    this.analyzeAndReturnUsagesCount(parameterName, scopeHolder);
                }
            }

            private void checkUseVariables(@NotNull List<Variable> variables, @NotNull PhpScopeHolder scopeHolder) {
                for (Variable variable : variables) {
                    final String parameterName = variable.getName();
                    if (StringUtils.isEmpty(parameterName)) {
                        continue;
                    }

                    PsiElement previous = variable.getPrevSibling();
                    if (previous instanceof PsiWhiteSpace) {
                        previous = previous.getPrevSibling();
                    }
                    if (null != previous && PhpTokenTypes.opBIT_AND == previous.getNode().getElementType()) {
                        final PhpAccessVariableInstruction[] usages = getVariableUsages(parameterName, scopeHolder);
                        if (0 == usages.length) {
                            holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }

                        continue;
                    }

                    final int variableUsages = this.analyzeAndReturnUsagesCount(parameterName, scopeHolder);
                    if (0 == variableUsages) {
                        holder.registerProblem(variable, messageUnused, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
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
                    final PsiElement parent = instruction.getAnchor().getParent();

                    if (parent instanceof ArrayAccessExpression) {
                        /* find out which expression is holder */
                        PsiElement objLastSemanticExpression = instruction.getAnchor();
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
                                /* when modifying non non-reference, register as write only access for reporting */
                                targetExpressions.add(parent);
                            }
                        }
                        if (parent.getParent().getClass() != StatementImpl.class) {
                            ++intCountReadAccesses;
                        }

                        continue;
                    }

                    /* if variable assigned with reference, we need to preserve this information for correct checks */
                    if (parent instanceof AssignmentExpression) {
                        /* ensure variable with the same name being written */
                        final AssignmentExpression referenceAssignmentCandidate = (AssignmentExpression) parent;
                        if (referenceAssignmentCandidate.getVariable() instanceof Variable) {
                            final Variable sameVariableCandidate = (Variable) referenceAssignmentCandidate.getVariable();
                            if (sameVariableCandidate.getName().equals(parameterName)) {
                                ++intCountWriteAccesses;
                                if (isReference) {
                                    /* when modifying the reference it's link READ and linked WRITE semantics */
                                    ++intCountReadAccesses;
                                }

                                /* now ensure operation is assignment of reference */
                                PsiElement operation = sameVariableCandidate.getNextSibling();
                                while (operation != null && operation.getNode().getElementType() != PhpTokenTypes.opASGN) {
                                    operation = operation.getNextSibling();
                                }
                                if (operation != null && operation.getText().replaceAll("\\s+", "").equals("=&")) {
                                    isReference = true;
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
                        targetExpressions.add(instruction.getAnchor());
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
        PhpEntryPointInstruction objEntryPoint = scopeHolder.getControlFlow().getEntryPoint();
        return PhpControlFlowUtil.getFollowingVariableAccessInstructions(objEntryPoint, parameterName, false);
    }
}