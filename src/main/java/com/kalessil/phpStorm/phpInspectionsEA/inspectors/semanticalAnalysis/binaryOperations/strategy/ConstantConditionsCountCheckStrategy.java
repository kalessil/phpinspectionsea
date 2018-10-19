package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ValueRange;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final public class ConstantConditionsCountCheckStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false.";

    private static final Set<IElementType> targetOperations      = new HashSet<>();
    private static final Map<String, ValueRange> targetFunctions = new HashMap<>();
    static {
        targetOperations.add(PhpTokenTypes.opEQUAL);
        targetOperations.add(PhpTokenTypes.opNOT_EQUAL);
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opGREATER);
        targetOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        targetOperations.add(PhpTokenTypes.opLESS);
        targetOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);

        targetFunctions.put("count",          ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("strlen",         ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("mb_strlen",      ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("iconv_strlen",   ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("preg_match",     ValueRange.of(0, 1L));
        targetFunctions.put("preg_match_all", ValueRange.of(0, Long.MAX_VALUE));
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (targetOperations.contains(operator)) {
            final PsiElement left = expression.getLeftOperand();
            if (OpenapiTypesUtil.isFunctionReference(left)) {
                final String functionName = ((FunctionReference) left).getName();
                if (functionName != null && targetFunctions.containsKey(functionName)) {
                    final PsiElement right = expression.getRightOperand();
                    if (right != null && OpenapiTypesUtil.isNumber(right)) {
                        Long number;
                        try {
                            number = Long.parseLong(right.getText());
                        } catch (final NumberFormatException wrongFormat) {
                            number = null;
                        }
                        if (number != null) {
                            final ValueRange range = targetFunctions.get(functionName);
                            if (operator == PhpTokenTypes.opLESS) {
                                if (result = (number <= range.getMinimum())) {
                                    holder.registerProblem(expression, String.format(messageAlwaysFalse, expression.getText()));
                                }
                            } else if (operator == PhpTokenTypes.opLESS_OR_EQUAL) {
                                if (result = (number < range.getMinimum())) {
                                    holder.registerProblem(expression, String.format(messageAlwaysFalse, expression.getText()));
                                }
                            } else if (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opIDENTICAL) {
                                if (result = (!range.isValidValue(number))) {
                                    holder.registerProblem(expression, String.format(messageAlwaysFalse, expression.getText()));
                                }
                            } else if (operator == PhpTokenTypes.opNOT_EQUAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                                if (result = (!range.isValidValue(number))) {
                                    holder.registerProblem(expression, String.format(messageAlwaysTrue, expression.getText()));
                                }
                            } else if (operator == PhpTokenTypes.opGREATER) {
                                if (result = (number < range.getMinimum())) {
                                    holder.registerProblem(expression, String.format(messageAlwaysTrue, expression.getText()));
                                }
                            } else if (operator == PhpTokenTypes.opGREATER_OR_EQUAL) {
                                if (result = (number <= range.getMinimum())) {
                                    holder.registerProblem(expression, String.format(messageAlwaysTrue, expression.getText()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
