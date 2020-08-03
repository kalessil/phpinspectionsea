package com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

final public class SuspiciousOperatorFormattingStrategy {
    private static final String messagePattern = "Probably '%o%' operator should be used here (or proper formatting applied).";

    private static final Map<IElementType, String> mapping = new HashMap<>();
    static {
        mapping.put(PhpTokenTypes.opPLUS,  "+=");
        mapping.put(PhpTokenTypes.opMINUS, "-=");
        mapping.put(PhpTokenTypes.opNOT,   "!=");
    }

    static public void apply(@NotNull AssignmentExpression expression, @NotNull ProblemsHolder holder) {
        final PhpPsiElement value = expression.getValue();
        if (value instanceof UnaryExpression) {
            /* previous should be "...=[!+-] ..." */
            final PsiElement previous = value.getPrevSibling();
            if (null == previous || PhpTokenTypes.opASGN != previous.getNode().getElementType()) {
                return;
            }
            final PsiElement valueOperator = ((UnaryExpression) value).getOperation();
            if (null == valueOperator || !(valueOperator.getNextSibling() instanceof PsiWhiteSpace)) {
                return;
            }

            /* analyze statement */
            final IElementType valueOperation = valueOperator.getNode().getElementType();
            if (mapping.containsKey(valueOperation)) {
                holder.registerProblem(
                        expression,
                        MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%o%", mapping.get(valueOperation)))
                );
            }
        }
    }
}
