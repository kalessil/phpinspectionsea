package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MultipleFalsyValuesCheckStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true when reached.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false when reached.";

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (operator != null && (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR)) {
            /* false-positives: part of another condition */
            final PsiElement parent  = expression.getParent();
            final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
            if (!(context instanceof BinaryExpression) || ((BinaryExpression) context).getOperationType() != operator) {
                final List<BinaryExpression> fragments = extractFragments(expression, operator);
                if (fragments.size() > 1) {
                    final Map<PsiElement, Boolean> falsyStates = new HashMap<>();
                    for (final BinaryExpression binary : fragments) {
                        final PsiElement right = binary.getRightOperand();
                        if (right != null) {
                            final PsiElement subject = isFalsyValue(right) ? binary.getLeftOperand() : right;
                            if (subject != null) {
                                final boolean isFalsyExpected      = binary.getOperationType() == PhpTokenTypes.opEQUAL;
                                final Optional<PsiElement> matched = falsyStates.keySet().stream()
                                        .filter(key -> OpenapiEquivalenceUtil.areEqual(key, subject))
                                        .findFirst();
                                if (matched.isPresent()) {
                                    result                    = true;
                                    final boolean isFalsySame = isFalsyExpected == falsyStates.get(matched.get());
                                    if (operator == PhpTokenTypes.opAND) {
                                        holder.registerProblem(
                                                binary,
                                                String.format(isFalsySame ? messageAlwaysTrue : messageAlwaysFalse, binary.getText()),
                                                isFalsySame ? ProblemHighlightType.LIKE_UNUSED_SYMBOL : ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                        );
                                    } else {
                                        holder.registerProblem(
                                                binary,
                                                String.format(isFalsySame ? messageAlwaysFalse : messageAlwaysTrue, binary.getText()),
                                                isFalsySame ? ProblemHighlightType.LIKE_UNUSED_SYMBOL : ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                        );
                                    }
                                } else {
                                    falsyStates.put(subject, isFalsyExpected);
                                }
                            }
                        }
                    }
                    falsyStates.clear();
                }
                fragments.clear();
            }
        }
        return result;
    }

    @NotNull
    private static List<BinaryExpression> extractFragments(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
        final List<BinaryExpression> result = new ArrayList<>();
        final IElementType binaryOperator   = binary.getOperationType();
        if (binaryOperator == operator) {
            Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                    .filter(Objects::nonNull).map(ExpressionSemanticUtil::getExpressionTroughParenthesis)
                    .forEach(expression -> {
                        if (expression instanceof BinaryExpression) {
                            result.addAll(extractFragments((BinaryExpression) expression, operator));
                        }
                    });
        } else if (binaryOperator == PhpTokenTypes.opEQUAL || binaryOperator == PhpTokenTypes.opNOT_EQUAL) {
            final boolean isFalsyCheck = Stream.of(binary.getRightOperand(), binary.getLeftOperand())
                    .filter(Objects::nonNull).anyMatch(MultipleFalsyValuesCheckStrategy::isFalsyValue);
            if (isFalsyCheck) {
                result.add(binary);
            }
        }
        return result;
    }

    private static boolean isFalsyValue(@NotNull PsiElement element) {
        if (element instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) element).getContents().isEmpty();
        } else if (element instanceof ConstantReference) {
            return PhpLanguageUtil.isFalse(element) || PhpLanguageUtil.isNull(element);
        } else if (element instanceof ArrayCreationExpression) {
            return element.getChildren().length == 0;
        } else if (OpenapiTypesUtil.isNumber(element)) {
            return element.getText().equals("0");
        }
        return false;
    }
}
