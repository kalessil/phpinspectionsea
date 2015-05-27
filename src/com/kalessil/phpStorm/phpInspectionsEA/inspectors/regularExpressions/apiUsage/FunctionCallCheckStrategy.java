package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.jetbrains.annotations.NotNull;

public class FunctionCallCheckStrategy {
    private static final String strProblemQuote = "Second parameter shall be provided (implicit delimiter definition)";
    private static final String strProblemMatchAll = "Probably 'preg_match()' can be used instead (3rd parameter not yet specified)";

    static public void apply(final String functionName, PsiElement[] params, @NotNull final FunctionReference reference, @NotNull final ProblemsHolder holder) {
        if (!StringUtil.isEmpty(functionName)) {

            if (functionName.equals("preg_quote") && 1 == params.length) {
                holder.registerProblem(reference, strProblemQuote, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                return;
            }

            if (functionName.equals("preg_match_all") && 2 == params.length) {
                holder.registerProblem(reference, strProblemMatchAll, ProblemHighlightType.WEAK_WARNING);
            }
        }
    }
}
