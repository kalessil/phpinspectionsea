package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PowerOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' can be used instead";

    @NotNull
    @Override
    public String getShortName() {
        return "PowerOperatorCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Power operator can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP560)) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("pow")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2) {
                            final boolean wrapBase   = arguments[0] instanceof BinaryExpression ||
                                                       arguments[0] instanceof TernaryExpression;
                            final boolean wrapPower  = arguments[1] instanceof BinaryExpression ||
                                                       arguments[1] instanceof TernaryExpression;
                            final String replacement =
                                    (reference.getParent() instanceof BinaryExpression ? "(%b% ** %p%)" : "%b% ** %p%")
                                            .replace("%p%", wrapPower ? "(%p%)" : "%p%" )
                                            .replace("%b%", wrapBase ? "(%b%)" : "%b%")
                                            .replace("%p%", arguments[1].getText())
                                            .replace("%b%", arguments[0].getText());
                            holder.registerProblem(
                                    reference,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    new UseTheOperatorFix(replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseTheOperatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use ** operator instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseTheOperatorFix(@NotNull String expression) {
            super(expression);
        }
    }
}
