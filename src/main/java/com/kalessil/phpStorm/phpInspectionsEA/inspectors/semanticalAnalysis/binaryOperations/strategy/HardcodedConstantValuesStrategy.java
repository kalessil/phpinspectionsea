package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class HardcodedConstantValuesStrategy {
    private static final String messageEnforces  = "This operand enforces the operation result.";
    private static final String messageSenseless = "This operand doesn't make any sense here.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final IElementType operation = expression.getOperationType();
        if (PhpTokenTypes.tsSHORT_CIRCUIT_AND_OPS.contains(operation)) {
            return analyzeAndOperation(expression.getLeftOperand(), expression.getRightOperand(), holder);
        }
        if (PhpTokenTypes.tsSHORT_CIRCUIT_OR_OPS.contains(operation)) {
            return analyzeOrOperation(expression.getLeftOperand(), expression.getRightOperand(), holder);
        }
        return false;
    }

    private static boolean analyzeAndOperation(
            @Nullable PsiElement left,
            @Nullable PsiElement right,
            @NotNull ProblemsHolder holder
    ) {
        return Stream.of(left, right).anyMatch(operand -> {
            if (PhpLanguageUtil.isFalse(operand) || PhpLanguageUtil.isNull(operand)) {
                holder.registerProblem(
                        operand,
                        MessagesPresentationUtil.prefixWithEa(messageEnforces)
                );
                return true;
            }
            if (PhpLanguageUtil.isTrue(operand)) {
                holder.registerProblem(
                        operand,
                        MessagesPresentationUtil.prefixWithEa(messageSenseless),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL
                );
                return true;
            }
            return false;
        });
    }

    private static boolean analyzeOrOperation(
            @Nullable PsiElement left,
            @Nullable PsiElement right,
            @NotNull ProblemsHolder holder
    ) {
        return Stream.of(left, right).anyMatch(operand -> {
            if (PhpLanguageUtil.isTrue(operand)) {
                holder.registerProblem(
                        operand,
                        MessagesPresentationUtil.prefixWithEa(messageEnforces)
                );
                return true;
            }
            if (PhpLanguageUtil.isFalse(operand) || PhpLanguageUtil.isNull(operand)) {
                holder.registerProblem(
                        operand,
                        MessagesPresentationUtil.prefixWithEa(messageSenseless),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL
                );
                return true;
            }
            return false;
        });
    }
}
