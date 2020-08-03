package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class FunctionCallCheckStrategy {
    private static final String messageQuote = "Second parameter should be provided (for proper symbols escaping).";
    private static final String messageMatch = "'preg_match(...)' would fit more here (also performs better).";

    static public void apply(@Nullable String functionName, @NotNull FunctionReference reference, @NotNull ProblemsHolder holder) {
        if (functionName != null && !functionName.isEmpty()) {
            final PsiElement[] arguments = reference.getParameters();
            if (arguments.length == 1 && functionName.equals("preg_quote")) {
                holder.registerProblem(
                        reference,
                        MessagesPresentationUtil.prefixWithEa(messageQuote)
                );
            } else if (arguments.length == 2 && functionName.equals("preg_match_all")) {
                if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                    holder.registerProblem(
                            reference,
                            MessagesPresentationUtil.prefixWithEa(messageMatch),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
            }
        }
    }
}
