package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class TypeUnsafeComparisonInspector extends BasePhpInspection {
    private static final String strProblemDescription = "'==' and '!=' are not type sensitive. " +
            "Hardening to '===' and '!==' will cover/point to types casting issues.";

    @NotNull
    public String getDisplayName() {
        return "API: type-unsafe comparison";
    }

    @NotNull
    public String getShortName() {
        return "TypeUnsafeComparisonInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                PsiElement objOperation = expression.getOperation();
                if (null == objOperation) {
                    return;
                }

                final String strOperation = objOperation.getText();
                if (
                    !strOperation.equals("==") &&
                    !strOperation.equals("!=")
                ) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}