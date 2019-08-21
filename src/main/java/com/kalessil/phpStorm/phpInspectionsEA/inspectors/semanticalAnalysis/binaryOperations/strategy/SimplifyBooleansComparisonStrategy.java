package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class SimplifyBooleansComparisonStrategy {
    private static final String messageIdentical    = "'(X && Y) || (!X && !Y)' -> '(bool) X === (bool) Y'";
    private static final String messageNotIdentical = "'(X && !Y) || (!X && Y)' -> '(bool) X !== (bool) Y'";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        // NOTE: not a nested one; see UnnecessaryEmptinessCheckInspector for details

        if (expression.getOperationType() == PhpTokenTypes.opOR) {
            final PsiElement left = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
            if (left instanceof BinaryExpression) {
                final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> leftParts = extract((BinaryExpression) left);
                if (leftParts != null) {
                    final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
                    if (right instanceof BinaryExpression) {
                        final Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> rightParts = extract((BinaryExpression) right);
                        if (rightParts != null) {
                            /* ensure same amount of arguments with and without inversion */
                            final int checkSum = Stream.of(leftParts.first.second, leftParts.second.second, rightParts.first.second, rightParts.second.second)
                                    .map(isInverted -> isInverted ? -1 : 1)
                                    .reduce(0, Integer::sum);
                            if (checkSum == 0) {

                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private static Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> extract(@NotNull BinaryExpression source) {
        Pair<Pair<PsiElement, Boolean>, Pair<PsiElement, Boolean>> result = null;
        if (source.getOperationType() == PhpTokenTypes.opAND) {
            final PsiElement left  = source.getLeftOperand();
            final PsiElement right = source.getRightOperand();
            if (left != null && right != null) {
                final List<Pair<PsiElement, Boolean>> parts = Stream.of(left, right)
                        .map(part -> {
                            final PsiElement falsyProbe = extractFalsyValue(part);
                            final PsiElement value      = falsyProbe != null ? falsyProbe : extractTruthyValue(part);
                            return value == null ? null : new Pair<>(value, falsyProbe != null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (parts.size() == 2) {
                    result = new Pair<>(parts.get(0), parts.get(1));
                }
                parts.clear();
            }
        }
        return result;
    }

    @Nullable
    private static PsiElement extractFalsyValue(@Nullable PsiElement expression) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context
            - ! ..., but not !empty(), !isset()
            - ... === false
            - ... == false
            - ... !== true
            - ... != true
            - yoda notation
        */
        return null;
    }

    @Nullable
    private static PsiElement extractTruthyValue(@Nullable PsiElement expression) {
        /* NOTE: ExpressionSemanticUtil.isUsedAsLogicalOperand is not applicable due to very specific context
            - ..., but not isset(), empty()
            - ... === true
            - ... == true
            - ... !== false
            - ... != false
            - (cast) ...
            - yoda notation
        */
        return null;
    }
}
