package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UnNecessaryDoubleQuotesInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Use single quotes instead";

    @NotNull
    public String getDisplayName() {
        return "Performance: unnecessary double quotes";
    }

    @NotNull
    public String getShortName() {
        return "UnNecessaryDoubleQuotesInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {
                String strValueWithQuotes = expression.getText();
                if (strValueWithQuotes.charAt(0) != '"') {
                    return;
                }

                final boolean hasInlineExpressions = (
                    strValueWithQuotes.contains("$") ||
                    strValueWithQuotes.contains("\\")
                );
                if (hasInlineExpressions) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
