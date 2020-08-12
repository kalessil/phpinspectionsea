package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ValueRange;
import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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

        targetFunctions.put("count",            ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("sizeof",           ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("filesize",         ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("strlen",           ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("mb_strlen",        ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("iconv_strlen",     ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("preg_match",       ValueRange.of(0, 1L));
        targetFunctions.put("preg_match_all",   ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("substr_count",     ValueRange.of(0, Long.MAX_VALUE));
        targetFunctions.put("mb_substr_count ", ValueRange.of(0, Long.MAX_VALUE));
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (targetOperations.contains(operator)) {
            final PsiElement left = expression.getLeftOperand();
            if (left != null && isTargetCall(left, holder)) {
                final PsiElement right = expression.getRightOperand();
                if (right != null && OpenapiTypesUtil.isNumber(right)) {
                    Long number;
                    try {
                        number = Long.parseLong(right.getText());
                    } catch (final NumberFormatException wrongFormat) {
                        number = null;
                    }
                    if (number != null) {
                        final ValueRange range = targetFunctions.get(((FunctionReference) left).getName());
                        if (operator == PhpTokenTypes.opLESS) {
                            if (result = (number <= range.getMinimum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            } else if (result = (number > range.getMaximum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            }
                        } else if (operator == PhpTokenTypes.opLESS_OR_EQUAL) {
                            if (result = (number < range.getMinimum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            } else if (result = (number >= range.getMaximum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            }
                        } else if (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opIDENTICAL) {
                            if (result = (!range.isValidValue(number))) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            }
                        } else if (operator == PhpTokenTypes.opNOT_EQUAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                            if (result = (!range.isValidValue(number))) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            }
                        } else if (operator == PhpTokenTypes.opGREATER) {
                            if (result = (number < range.getMinimum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            } else if (result = (number >= range.getMaximum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            }
                        } else if (operator == PhpTokenTypes.opGREATER_OR_EQUAL) {
                            if (result = (number <= range.getMinimum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                                );
                            } else if (result = (number > range.getMaximum())) {
                                holder.registerProblem(
                                        expression,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                                );
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isTargetCall(@NotNull PsiElement candidate, @NotNull ProblemsHolder holder) {
        boolean result = false;
        if (candidate instanceof FunctionReference) {
            final FunctionReference reference = (FunctionReference) candidate;
            final String functionName         = reference.getName();
            if (functionName != null) {
                if (reference instanceof MethodReference) {
                    result = (functionName.equals("count") || functionName.equals("sizeof")) && isImplementingCountable((MethodReference) reference, holder);
                } else {
                    result = targetFunctions.containsKey(functionName);
                }
            }
        }
        return result;
    }

    private static boolean isImplementingCountable(@NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        boolean result        = true;
        final PsiElement base = reference.getFirstChild();
        if (base instanceof PhpTypedElement) {
            final Project project = holder.getProject();
            final PhpType type    = OpenapiResolveUtil.resolveType((PhpTypedElement) base, project);
            if (type != null) {
                final PhpIndex index = PhpIndex.getInstance(project);
                result = type.filterUnknown().getTypes().stream().anyMatch(t -> {
                    final String normalized = Types.getType(t);
                    if (normalized.startsWith("\\")) {
                        final Collection<PhpClass> resolved = OpenapiResolveUtil.resolveClassesByFQN(normalized, index);
                        if (!resolved.isEmpty()) {
                            return InterfacesExtractUtil.getCrawlInheritanceTree(resolved.iterator().next(), false).stream()
                                    .anyMatch(parent -> parent.getFQN().equals("\\Countable"));
                        }
                    }
                    return false;
                });
            }
        }
        return result;
    }
}
