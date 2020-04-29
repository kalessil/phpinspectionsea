package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ConstantConditionsPhpVersionStrategy {
    private static final String messageAlwaysTrue  = "'%s' seems to be always true.";
    private static final String messageAlwaysFalse = "'%s' seems to be always false.";

    private static final Map<String, PhpLanguageLevel> versionsMapping = new HashMap<>();
    static {
        // versionsMapping.put("70200", PhpLanguageLevel.PHP720); will require bumping PS minimal supported version
        versionsMapping.put("70100", PhpLanguageLevel.PHP710);
        versionsMapping.put("70000", PhpLanguageLevel.PHP700);
        versionsMapping.put("50600", PhpLanguageLevel.PHP560);
        versionsMapping.put("50500", PhpLanguageLevel.PHP550);
        versionsMapping.put("50400", PhpLanguageLevel.PHP540);
        versionsMapping.put("50300", PhpLanguageLevel.PHP530);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result        = false;
        final PsiElement left = expression.getLeftOperand();
        if (left instanceof ConstantReference && "PHP_VERSION_ID".equals(((ConstantReference) left).getName())) {
            final PsiElement right = expression.getRightOperand();
            if (right != null && OpenapiTypesUtil.isNumber(right)) {
                final String checkedVersion = right.getText();
                if (versionsMapping.containsKey(checkedVersion)) {
                    final PhpLanguageLevel checked = versionsMapping.get(checkedVersion);
                    final PhpLanguageLevel current = PhpLanguageLevel.get(holder.getProject());
                    if (checked.below(current)) {
                        final IElementType operator = expression.getOperationType();
                        /* the checked version is below current, inspect constant conditions */
                        if (result = (operator == PhpTokenTypes.opEQUAL || operator == PhpTokenTypes.opIDENTICAL)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                            );
                        } else if (result = (operator == PhpTokenTypes.opNOT_EQUAL || operator == PhpTokenTypes.opNOT_IDENTICAL)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                            );
                        } else if (result = (operator == PhpTokenTypes.opGREATER || operator == PhpTokenTypes.opGREATER_OR_EQUAL)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                            );
                        } else if (result = (operator == PhpTokenTypes.opLESS || operator == PhpTokenTypes.opLESS_OR_EQUAL)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                            );
                        }
                    } else if (checked == current) {
                        final IElementType operator = expression.getOperationType();
                        if (result = (operator == PhpTokenTypes.opGREATER_OR_EQUAL)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysTrue), expression.getText())
                            );
                        } else if (result = (operator == PhpTokenTypes.opLESS)) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messageAlwaysFalse), expression.getText())
                            );
                        }
                    }
                }
            }
        }
        return result;
    }
}
