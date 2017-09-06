package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ForeachInvariantsInspector extends BasePhpInspection {
    private static final String foreachInvariant = "Foreach can probably be used instead (easier to read and support; ensure a string is not iterated).";
    private static final String eachFunctionUsed = "Foreach should be used instead (8x faster).";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFor(@NotNull For forStatement) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(forStatement);
                if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                    final PsiElement variable = this.getCounterVariable(forStatement);
                    if (variable != null) {
                        final boolean result =
                                this.isCounterVariableIncremented(forStatement, variable) &&
                                this.isCheckedAsExpected(forStatement, variable) &&
                                this.isUsedInArrayAccess(body, variable);
                        if (result) {
                            holder.registerProblem(forStatement.getFirstChild(), foreachInvariant);
                        }
                    }
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignmentExpression) {
                PsiElement value = assignmentExpression.getValue();
                if (OpenapiTypesUtil.is(value, PhpElementTypes.EXPRESSION)) {
                    value = value.getFirstChild();
                }

                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final String functionName = ((FunctionReference) value).getName();
                    if (functionName != null && functionName.equals("each")) {
                        final PsiElement parent = assignmentExpression.getParent();
                        if (parent instanceof While || parent instanceof For) {
                            holder.registerProblem(parent.getFirstChild(), eachFunctionUsed, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }

            @Nullable
            private PsiElement getCounterVariable(@NotNull For expression) {
                PsiElement result = null;
                for (final PhpPsiElement init : expression.getInitialExpressions()) {
                    if (OpenapiTypesUtil.isAssignment(init)) {
                        final AssignmentExpression assignment = (AssignmentExpression) init;
                        final PsiElement value                = assignment.getValue();
                        final PsiElement variable             = assignment.getVariable();
                        if (value != null && variable instanceof Variable && value.getText().equals("0")) {
                            result = variable;
                            break;
                        }
                    }
                }
                return result;
            }

            private boolean isCounterVariableIncremented(@NotNull For expression, @NotNull PsiElement variable) {
                boolean result = false;
                for (final PsiElement repeat : expression.getRepeatedExpressions()) {
                    if (repeat instanceof UnaryExpression) {
                        final UnaryExpression incrementCandidate = (UnaryExpression) repeat;
                        final PsiElement argument                = incrementCandidate.getValue();
                        if (
                            OpenapiTypesUtil.is(incrementCandidate.getOperation(), PhpTokenTypes.opINCREMENT) &&
                            argument != null && PsiEquivalenceUtil.areElementsEquivalent(variable, argument)
                        ) {
                            result = true;
                            break;
                        }
                    }
                }
                return result;
            }

            private boolean isUsedInArrayAccess(@NotNull GroupStatement body, @NotNull PsiElement variable) {
                boolean result = false;
                for (final ArrayAccessExpression offset : PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class)) {
                    final ArrayIndex index = offset.getIndex();
                    final PsiElement value = index == null ? null : index.getValue();
                    if (value instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, value)) {
                        result = true;
                        break;
                    }
                }
                return result;
            }

            private boolean isCheckedAsExpected(@NotNull For expression, @NotNull PsiElement variable) {
                boolean result = false;
                for (final PsiElement check : expression.getConditionalExpressions()) {
                    if (check instanceof BinaryExpression) {
                        final BinaryExpression condition = (BinaryExpression) check;
                        final PsiElement left            = condition.getLeftOperand();
                        final PsiElement right           = condition.getRightOperand();

                        final PsiElement value;
                        if (left instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, left)) {
                            value = right;
                        } else if (right instanceof Variable && PsiEquivalenceUtil.areElementsEquivalent(variable, right)) {
                            value = left;
                        } else {
                            value = null;
                        }

                        result = value instanceof Variable;
                        break;
                    }
                }
                return result;
            }
        };
    }
}