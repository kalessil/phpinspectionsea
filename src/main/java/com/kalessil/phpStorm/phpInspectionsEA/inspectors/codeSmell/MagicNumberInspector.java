package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.BinaryExpressionUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ElementTypeUtil;

import org.jetbrains.annotations.NotNull;

public class MagicNumberInspector extends BasePhpInspection {
    private static final String message = "Magic number should be replaced by a constant.";

    @NotNull
    public String getShortName() {
        return "MagicNumberInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpExpression(final PhpExpression expression) {
                if (isNumeric(expression)) {
                    if (!(expression.getParent() instanceof PhpReturn)) {
                        return;
                    }

                    registerProblem(expression);
                }
            }

            @Override
            public void visitPhpBinaryExpression(final BinaryExpression expression) {
                if (!BinaryExpressionUtil.isComparison(expression)) {
                    return;
                }

                if (isNumeric(expression.getLeftOperand()) &&
                    !isCounting(expression)) {
                    registerProblem(expression.getLeftOperand());
                }

                if (isNumeric(expression.getRightOperand()) &&
                    !isCounting(expression)) {
                    registerProblem(expression.getRightOperand());
                }
            }

            @Override
            public void visitPhpSwitch(final PhpSwitch switchStatement) {
                for (final PhpCase switchCase : switchStatement.getCases()) {
                    final PhpPsiElement caseCondition = switchCase.getCondition();

                    if (isNumeric(caseCondition)) {
                        registerProblem(caseCondition);
                    }
                }
            }

            @Override
            public void visitPhpField(final Field field) {
                if (!field.isConstant()) {
                    final PsiElement fieldValue = field.getDefaultValue();

                    if (fieldValue == null) {
                        return;
                    }

                    if (isNumeric(fieldValue) &&
                        !"0".equals(fieldValue.getText())) {
                        registerProblem(fieldValue);
                    }
                }
            }

            private boolean isNumeric(final PsiElement expression) {
                PsiElement testingExpression = expression;

                if (testingExpression instanceof UnaryExpression) {
                    testingExpression = ((UnaryExpression) testingExpression).getValue();
                }

                return (testingExpression instanceof PhpExpressionImpl) &&
                       (testingExpression.getNode().getElementType() == PhpElementTypes.NUMBER);
            }

            private boolean isCounting(@NotNull final BinaryExpression binaryExpression) {
                final IElementType operationType = binaryExpression.getOperationType();

                if (operationType == null) {
                    return false;
                }

                final boolean    numericOnOpposite = isNumeric(binaryExpression.getRightOperand());
                final PsiElement numericOperand    = numericOnOpposite ? binaryExpression.getRightOperand() : binaryExpression.getLeftOperand();

                if (numericOperand == null) {
                    return false;
                }

                if (operationType.equals(PhpTokenTypes.opIDENTICAL) ||
                    operationType.equals(PhpTokenTypes.opNOT_IDENTICAL) ||
                    operationType.equals(PhpTokenTypes.opEQUAL) ||
                    operationType.equals(PhpTokenTypes.opNOT_EQUAL)) {
                    return isBinaryNumeric(numericOperand.getText());
                }

                final IElementType normalizeOperationType = numericOnOpposite ? operationType : ElementTypeUtil.rotateOperation(operationType);

                return (normalizeOperationType.equals(PhpTokenTypes.opGREATER) ||
                        normalizeOperationType.equals(PhpTokenTypes.opGREATER_OR_EQUAL)) &&
                       isBinaryNumeric(numericOperand.getText());

            }

            private boolean isBinaryNumeric(@NotNull final String numericValue) {
                return "0".equals(numericValue) ||
                       "1".equals(numericValue);
            }

            private void registerProblem(final PsiElement rightOperand) {
                problemsHolder.registerProblem(rightOperand, message, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
