package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NestedNotOperatorsInspector extends BasePhpInspection {
    private static final String messagePattern = "Can be replaced with '%s'.";

    @NotNull
    @Override
    public String getShortName() {
        return "NestedNotOperatorsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Nested not operators usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpUnaryExpression(@NotNull UnaryExpression expression) {
                /* process ony not operations */
                if (!OpenapiTypesUtil.is(expression.getOperation(), PhpTokenTypes.opNOT)) {
                    return;
                }

                /* process only deepest not-operator: get contained expression */
                final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getValue());
                if (null == value) {
                    return;
                }
                /* if contained expression is also inversion, do nothing -> to not report several times */
                if (value instanceof UnaryExpression) {
                    if (OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.opNOT)) {
                        return;
                    }
                }

                /* check nesting level */
                PsiElement target = null;
                int nestingLevel  = 1;
                PsiElement parent = expression.getParent();
                while (parent instanceof UnaryExpression || parent instanceof ParenthesizedExpression) {
                    if (!(parent instanceof ParenthesizedExpression)) {
                        expression = (UnaryExpression) parent;
                        if (OpenapiTypesUtil.is(expression.getOperation(), PhpTokenTypes.opNOT)) {
                            ++nestingLevel;
                            target = parent;
                        }
                    }
                    parent = parent.getParent();
                }

                if (nestingLevel > 1) {
                    final String subject     = String.format(value instanceof BinaryExpression ? "(%s)" : "%s", value.getText());
                    final String replacement = String.format(nestingLevel % 2 == 0 ? "(bool) %s" : "! %s", subject);
                    holder.registerProblem(
                            target,
                            MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, replacement)),
                            nestingLevel % 2 == 0 ? new UseCastingLocalFix(replacement) : new UseSingleNotLocalFix(replacement)
                    );
                }
            }
        };
    }

    private static final class UseSingleNotLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use a single not operator";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseSingleNotLocalFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseCastingLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use boolean casting";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseCastingLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
