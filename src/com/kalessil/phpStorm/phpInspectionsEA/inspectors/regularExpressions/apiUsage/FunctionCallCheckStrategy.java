package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class FunctionCallCheckStrategy {
    private static final String strProblemQuote = "Second parameter should be provided (implicit delimiter definition)";
    private static final String strProblemMatchAll = "'preg_match()' can be used instead";

    static public void apply(final String functionName, @NotNull final FunctionReference reference, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(functionName)) {
            final PsiElement[] params = reference.getParameters();

            if (1 == params.length && functionName.equals("preg_quote")) {
                holder.registerProblem(reference, strProblemQuote, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                return;
            }

            if (
                2 == params.length && functionName.equals("preg_match_all") &&
                ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)
            ) {
                holder.registerProblem(reference, strProblemMatchAll, ProblemHighlightType.WEAK_WARNING);
                // return
            }
        }
    }
}
