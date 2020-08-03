package com.kalessil.phpStorm.phpInspectionsEA.inspectors;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Include;
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

public class PreloadingUsageCorrectnessInspector extends BasePhpInspection {
    private static final String message = "Perhaps it should be used 'opcache_compile_file()' here. See https://bugs.php.net/bug.php?id=78918 for details.";

    @NotNull
    @Override
    public String getShortName() {
        return "PreloadingUsageCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Preloading usage correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                if (include.getContainingFile().getName().equals("preload.php")) {
                    final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(include.getArgument());
                    if (argument != null && OpenapiTypesUtil.isStatementImpl(include.getParent())) {
                        holder.registerProblem(
                                include,
                                MessagesPresentationUtil.prefixWithEa(message),
                                new UseOpcacheCompileFileFix(String.format("opcache_compile_file(%s)", argument.getText()))
                        );
                    }
                }
            }
        };
    }

    private static final class UseOpcacheCompileFileFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'opcache_compile_file(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseOpcacheCompileFileFix(@NotNull String expression) {
            super(expression);
        }
    }
}
