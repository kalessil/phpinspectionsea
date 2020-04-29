package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ImplodeArgumentsOrderInspector extends BasePhpInspection {
    private static final String message = "The glue argument should be the first one.";

    @NotNull
    @Override
    public String getShortName() {
        return "ImplodeArgumentsOrderInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'implode(...)' arguments order";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("implode")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && arguments[1] instanceof StringLiteralExpression) {
                        final String replacement = String.format(
                                "%simplode(%s, %s)",
                                reference.getImmediateNamespaceName(),
                                arguments[1].getText(),
                                arguments[0].getText()
                        );
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(message),
                                new ReorderArgumentsFixer(replacement)
                        );
                    }
                }
            }
        };
    }

    private static final class ReorderArgumentsFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Reorder arguments";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        ReorderArgumentsFixer(@NotNull String expression) {
            super(expression);
        }
    }
}
