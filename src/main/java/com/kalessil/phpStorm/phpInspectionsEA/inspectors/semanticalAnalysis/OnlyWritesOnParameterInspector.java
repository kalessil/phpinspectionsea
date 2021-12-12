package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    // Inspection options.
    public boolean IGNORE_INCLUDES = true;

    private static final String messageOnlyWrites = "Parameter/variable is overridden, but is never used or appears outside of the scope.";
    private static final String messageUnused     = "The variable seems to be not used.";

    @NotNull
    @Override
    public String getShortName() {
        return "OnlyWritesOnParameterInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Parameter/variable is not used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (!method.isAbstract()) {
                    this.visitPhpFunction(method);
                }
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                Arrays.stream(function.getParameters())
                        .filter(parameter  -> !parameter.getName().isEmpty() && !parameter.isPassByRef())
                        .filter(parameter  -> {
                            final PhpType declaredType = OpenapiResolveUtil.resolveDeclaredType(parameter).filterUnknown().filterNull();
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
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignment) {
                /* because this hook fired e.g. for `.=` assignments (a BC break by PhpStorm) */
                if (OpenapiTypesUtil.isAssignment(assignment)) {
                    final PsiElement variable = assignment.getVariable();
                    if (variable instanceof Variable) {
                        /* false-positives: predefined and global variables */
                        final String variableName = ((Variable) variable).getName();
                        if (variableName.isEmpty() || ExpressionCostEstimateUtil.predefinedVars.contains(variableName)) {
                            return;
                        }
                        /* filter target contexts: we are supporting only certain of them */
                        final PsiElement parent = assignment.getParent();
                        final boolean isTargetContext =
                            parent instanceof ParenthesizedExpression ||
                            parent instanceof ArrayIndex ||
                            (parent instanceof BinaryExpression && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(((BinaryExpression) parent).getOperationType())) ||
                            OpenapiTypesUtil.isStatementImpl(parent) ||
                            OpenapiTypesUtil.isAssignment(parent);
                        if (isTargetContext) {
                            final Function scope = ExpressionSemanticUtil.getScope(assignment);
                            if (scope != null && Arrays.stream(scope.getParameters()).noneMatch(p -> p.getName().equals(variableName))) {
                                final List<Variable> uses   = ExpressionSemanticUtil.getUseListVariables(scope);
                                final boolean isUseVariable = uses != null && uses.stream().anyMatch(u -> u.getName().equals(variableName));
                                if (!isUseVariable) {
                                    this.analyzeAndReturnUsagesCount(variableName, scope);
                                }
                            }
                        }
                    }
                }
            }

            private void checkUseVariables(@NotNull List<Variable> variables, @NotNull Function function) {
                for (final Variable variable : variables) {
                    final String parameterName = variable.getName();
                    if (!parameterName.isEmpty()) {
                        PsiElement previous = variable.getPrevSibling();
                        if (previous instanceof PsiWhiteSpace) {
                            previous = previous.getPrevSibling();
                        }

                        if (OpenapiTypesUtil.is(previous, PhpTokenTypes.opBIT_AND)) {
                            if (OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(function.getControlFlow().getEntryPoint(), parameterName).isEmpty()) {
                                holder.registerProblem(
                                        variable,
                                        MessagesPresentationUtil.prefixWithEa(messageUnused),
                                        ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                );
                            }
                        } else if (this.analyzeAndReturnUsagesCount(parameterName, function) == 0) {
                            holder.registerProblem(
                                    variable,
                                    MessagesPresentationUtil.prefixWithEa(messageUnused),
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                            );
                        }
                    }
                }
            }

            private int analyzeAndReturnUsagesCount(@NotNull String parameterName, @NotNull Function function) {
                final List<PhpAccessVariableInstruction> usages = OpenapiControlFlowUtil.getFollowingVariableAccessInstructions(function.getControlFlow().getEntryPoint(), parameterName);
                if (usages.isEmpty()) {
                    return 0;
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
                            final PsiElement operation = ((UnaryExpression) objTopSemanticExpression).getOperation();
                            if (
                                OpenapiTypesUtil.is(operation, PhpTokenTypes.opINCREMENT) ||
                                OpenapiTypesUtil.is(operation, PhpTokenTypes.opDECREMENT)
                            ) {
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
                        if (
                            OpenapiTypesUtil.is(operation, PhpTokenTypes.opINCREMENT) ||
                            OpenapiTypesUtil.is(operation, PhpTokenTypes.opDECREMENT)
                        ) {
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
                        /* check if the target used as a container */
                        final PsiElement assignmentVariableCandidate = referenceAssignmentCandidate.getVariable();
                        if (assignmentVariableCandidate instanceof Variable) {
                            final Variable candidate = (Variable) assignmentVariableCandidate;
                            if (candidate.getName().equals(parameterName)) {
                                ++intCountWriteAccesses;
                                if (isReference) {
                                    /* when modifying the reference it's link READ and linked WRITE semantics */
                                    ++intCountReadAccesses;
                                }
                                /* now ensure operation is assignment of reference */
                                if (OpenapiTypesUtil.isAssignmentByReference(referenceAssignmentCandidate)) {
                                    isReference = true;
                                }
                                /* false-negative: inline assignment result has been used */
                                if (usages.size() == 2 && usages.get(0).getAnchor() == usages.get(1).getAnchor()) {
                                    holder.registerProblem(
                                            assignmentVariableCandidate,
                                            MessagesPresentationUtil.prefixWithEa(messageUnused),
                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                    );
                                    return 1;
                                }
                                continue;
                            }
                        }
                        /* check if the target used as a value */
                        final PsiElement assignmentValueCandidate = referenceAssignmentCandidate.getValue();
                        if (assignmentValueCandidate instanceof Variable) {
                            final Variable candidate = (Variable) assignmentValueCandidate;
                            if (candidate.getName().equals(parameterName)) {
                                ++intCountReadAccesses;
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


                if (intCountReadAccesses == 0 && intCountWriteAccesses > 0 && !this.isAnySuppressed(targetExpressions)) {
                    final boolean report = IGNORE_INCLUDES || !this.hasIncludes(function);
                    if (report) {
                        for (final PsiElement targetExpression : new HashSet<>(targetExpressions)) {
                            holder.registerProblem(
                                    targetExpression,
                                    MessagesPresentationUtil.prefixWithEa(messageOnlyWrites),
                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                            );
                        }
                    }
                }
                targetExpressions.clear();

                return usages.size();
            }

            private boolean isAnySuppressed(@NotNull List<PsiElement> expressions) {
                for (final PsiElement one : expressions) {
                    final PsiElement parent = one.getParent();
                    if (parent instanceof AssignmentExpression) {
                        final PsiElement grandParent = parent.getParent();
                        if (OpenapiTypesUtil.isStatementImpl(grandParent)) {
                            final PsiElement previous = ((PhpPsiElement) grandParent).getPrevPsiSibling();
                            if (previous instanceof PhpDocComment) {
                                final String candidate = previous.getText();
                                if (candidate.contains("@noinspection") && candidate.contains(getShortName())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            private boolean hasIncludes(@NotNull Function function) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null) {
                    return PsiTreeUtil.findChildOfType(body, Include.class) != null;
                }
                return false;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
                component.addCheckbox("Ignore 'include' and 'require' statements", IGNORE_INCLUDES, (isSelected) -> IGNORE_INCLUDES = isSelected)
        );
    }
}