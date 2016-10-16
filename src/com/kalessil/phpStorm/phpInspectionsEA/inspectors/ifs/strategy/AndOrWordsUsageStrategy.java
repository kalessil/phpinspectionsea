package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.impl.BinaryExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final public class AndOrWordsUsageStrategy {
    private final static String messagePattern = "'%o%' should be used instead (best practices)";

    static public void apply(PsiElement condition, @NotNull ProblemsHolder holder) {
        final Collection<BinaryExpressionImpl> expressions = PsiTreeUtil.findChildrenOfType(condition, BinaryExpressionImpl.class);
        /* don't forget to inspect top-level condition ;) */
        if (condition instanceof BinaryExpressionImpl) {
            expressions.add((BinaryExpressionImpl) condition);
        }

        if (expressions.size() > 0) {
            for (BinaryExpressionImpl expression : expressions) {
                final PsiElement operation = expression.getOperation();
                if (null == operation) {
                    continue;
                }

                final String operator = operation.getText().trim();
                if (operator.equals("and")) {
                    final String message = messagePattern.replace("%o%", "&&");
                    holder.registerProblem(operation, message, ProblemHighlightType.WEAK_WARNING);

                    continue;
                }

                if (operator.equals("or")) {
                    final String message = messagePattern.replace("%o%", "||");
                    holder.registerProblem(operation, message, ProblemHighlightType.WEAK_WARNING);

                    //continue;
                }
            }

            expressions.clear();
        }
    }
}
