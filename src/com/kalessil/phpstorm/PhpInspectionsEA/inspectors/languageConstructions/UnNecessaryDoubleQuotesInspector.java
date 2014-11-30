package com.kalessil.phpstorm.PhpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpstorm.PhpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;

public class UnNecessaryDoubleQuotesInspector extends BasePhpInspection {
    public static final String strProblemDescription = "Use single quotes instead";

    @NotNull
    public String getDisplayName() {
        return "API: unnecessary double quotes";
    }

    @NotNull
    public String getShortName() {
        return "UnNecessaryDoubleQuotesInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {
                String strValueWithQuotes = expression.getText();
                if (!strValueWithQuotes.startsWith("\"")) {
                    return;
                }

                final boolean hasVariablesInside = strValueWithQuotes.contains("$");
                if (hasVariablesInside) {
                    return;
                }

                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
