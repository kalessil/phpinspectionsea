package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

/* TODO: === and !== on strings/numbers/constants (constant expressions) */
/* ... == ... && ... == ... */
/* ... != ... || ... != ... */

final public class MultipleValuesEqualityStrategy {

    private static final Set<IElementType> equalOperators    = new HashSet<>();
    private static final Set<IElementType> notEqualOperators = new HashSet<>();
    static {
        equalOperators.add(PhpTokenTypes.opEQUAL);
        equalOperators.add(PhpTokenTypes.opIDENTICAL);

        notEqualOperators.add(PhpTokenTypes.opNOT_EQUAL);
        notEqualOperators.add(PhpTokenTypes.opNOT_IDENTICAL);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator != null && (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR)) {
            /* false-positives: part of another condition */
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<PsiElement> fragments = extractFragments(expression, operator);
                if (fragments.size() > 1) {
                    final Set<IElementType> operators     = operator == PhpTokenTypes.opAND ? equalOperators : notEqualOperators;
                    final List<BinaryExpression> filtered = fragments.stream()
                            .filter(fragment -> fragment instanceof BinaryExpression)
                            .map(fragment    -> (BinaryExpression) fragment)
                            .filter(fragment -> operators.contains(fragment.getOperationType()))
                            .collect(Collectors.toList());
                    if (filtered.size() > 1) {
                        result = analyze(filtered, operator, holder);
                    }
                    filtered.clear();
                }
                fragments.clear();
            }
        }
        return result;
    }

    private static boolean analyze(
            @NotNull List<BinaryExpression> filtered,
            @NotNull IElementType operator,
            @NotNull ProblemsHolder holder
    ) {
        return false;
    }

    @NotNull
    private static List<PsiElement> extractFragments(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
        final List<PsiElement> result = new ArrayList<>();
        if (binary.getOperationType() == operator) {
            Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                    .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                    .forEach(expression -> {
                        if (expression instanceof BinaryExpression) {
                            result.addAll(extractFragments((BinaryExpression) expression, operator));
                        } else {
                            result.add(expression);
                        }
                    });
        } else {
            result.add(binary);
        }
        return result;
    }
}
