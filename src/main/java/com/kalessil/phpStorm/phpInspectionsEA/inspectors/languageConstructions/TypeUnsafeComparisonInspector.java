package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ClassInStringContextStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.utils.strategy.ComparableCoreClassesStrategy;
import org.jetbrains.annotations.NotNull;

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    private static final String strProblemDescription                      = "Hardening to type safe '===', '!==' will cover/point to types casting issues.";
    private static final String strProblemDescriptionSafeToReplace         = "Safely use '... === ...', '... !== ...' constructions instead.";
    private static final String strProblemDescriptionMissingToStringMethod = "Class %class% must implement __toString().";

    @NotNull
    public String getShortName() {
        return "TypeUnsafeComparisonInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                /* verify operation is as expected */
                final PsiElement objOperation = expression.getOperation();
                if (null == objOperation) {
                    return;
                }
                final IElementType operationType = objOperation.getNode().getElementType();
                if (operationType != PhpTokenTypes.opEQUAL && operationType != PhpTokenTypes.opNOT_EQUAL) {
                    return;
                }

                this.triggerProblem(expression);
            }

            /** generates more specific warnings for given expression */
            private void triggerProblem(BinaryExpression objExpression) {
                final PsiElement objLeftOperand  = objExpression.getLeftOperand();
                final PsiElement objRightOperand = objExpression.getRightOperand();
                if (
                    objRightOperand instanceof StringLiteralExpression ||
                    objLeftOperand instanceof StringLiteralExpression
                ) {
                    PsiElement objNonStringOperand;
                    String strLiteralValue;
                    if (objRightOperand instanceof StringLiteralExpression) {
                        strLiteralValue     = ((StringLiteralExpression) objRightOperand).getContents();
                        objNonStringOperand = objLeftOperand;
                    } else {
                        strLiteralValue     = ((StringLiteralExpression) objLeftOperand).getContents();
                        objNonStringOperand = objRightOperand;
                    }


                    /* resolve 2nd operand type, if class ensure __toString is implemented */
                    objNonStringOperand = ExpressionSemanticUtil.getExpressionTroughParenthesis(objNonStringOperand);
                    if (
                        null != objNonStringOperand &&
                        ClassInStringContextStrategy.apply(objNonStringOperand, holder, objExpression, strProblemDescriptionMissingToStringMethod)
                    ) {
                        /* TODO: weak warning regarding under-the-hood string casting */
                        return;
                    }


                    if (strLiteralValue.length() > 0 && !strLiteralValue.matches("^[0-9\\+\\-]+$")) {
                        holder.registerProblem(objExpression, strProblemDescriptionSafeToReplace, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        return;
                    }
                }

                /* some of objects supporting direct comparison: search for .compare_objects in PHP sources */
                if (ComparableCoreClassesStrategy.apply(objLeftOperand, objRightOperand, holder)) {
                    return;
                }

                holder.registerProblem(objExpression, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}