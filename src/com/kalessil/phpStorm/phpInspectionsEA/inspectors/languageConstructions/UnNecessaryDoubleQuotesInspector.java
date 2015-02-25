package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class UnNecessaryDoubleQuotesInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Safely use single quotes instead";

    @NotNull
    public String getShortName() {
        return "UnNecessaryDoubleQuotesInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpStringLiteralExpression(StringLiteralExpression expression) {
                String strValueWithQuotes = expression.getText();
                if (
                    strValueWithQuotes.charAt(0) != '"' ||
                    strValueWithQuotes.indexOf('$') >= 0 ||
                    strValueWithQuotes.indexOf('\\') >= 0 ||
                    (strValueWithQuotes.length() == 3 && strValueWithQuotes.equals("\"'\""))
                ) {
                    return;
                }

                if (!(ExpressionSemanticUtil.getBlockScope(expression) instanceof PhpDocComment)) {
                    holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
