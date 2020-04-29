package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class AndOrWordsUsageStrategy {
    private final static String messagePattern = "'%o%' should be used instead (best practices).";

    static public void apply(PsiElement condition, @NotNull ProblemsHolder holder) {
        final Collection<BinaryExpression> expressions = PsiTreeUtil.findChildrenOfType(condition, BinaryExpression.class);
        /* don't forget to inspect top-level condition ;) */
        if (condition instanceof BinaryExpression) {
            expressions.add((BinaryExpression) condition);
        }

        if (!expressions.isEmpty()) {
            for (BinaryExpression expression : expressions) {
                final PsiElement operation = expression.getOperation();
                if (null == operation) {
                    continue;
                }

                final String operator = operation.getText().trim();
                if (operator.equalsIgnoreCase("and")) {
                    holder.registerProblem(
                            operation,
                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%o%", "&&")),
                            ProblemHighlightType.WEAK_WARNING
                    );

                    continue;
                }

                if (operator.equalsIgnoreCase("or")) {
                    holder.registerProblem(
                            operation,
                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%o%", "||")),
                            ProblemHighlightType.WEAK_WARNING
                    );
                    //continue;
                }
            }

            expressions.clear();
        }
    }
}
