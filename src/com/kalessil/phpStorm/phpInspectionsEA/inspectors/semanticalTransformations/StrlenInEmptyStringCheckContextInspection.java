package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrlenInEmptyStringCheckContextInspection extends BasePhpInspection {
    private static final String strProblemDescription = "Can be replaced by comparing with empty string";

    @NotNull
    public String getDisplayName() {
        return "Semantics: strlen in empty string check context";
    }

    @NotNull
    public String getShortName() {
        return "StrlenInEmptyStringCheckContextInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression)  {
                PsiElement objRightOperand = expression.getRightOperand();
                if (
                    !(objRightOperand instanceof PhpExpression) ||
                    !(objRightOperand.getNode().getElementType() == PhpElementTypes.NUMBER) ||
                    !objRightOperand.getText().equals("0")
                ) {
                    return;
                }


                PsiElement objLeftOperand = expression.getLeftOperand();
                //noinspection ConstantConditions
                if (
                    !(objLeftOperand instanceof FunctionReference) ||
                    null == ((FunctionReference) objLeftOperand).getName() ||
                    !((FunctionReference) objLeftOperand).getName().toLowerCase().equals("strlen")
                ) {
                    return;
                }


                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}