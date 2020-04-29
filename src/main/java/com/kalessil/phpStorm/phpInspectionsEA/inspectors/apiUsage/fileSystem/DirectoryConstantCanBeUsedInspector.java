package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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

public class DirectoryConstantCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "'__DIR__' should be used instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "DirectoryConstantCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "__DIR__ constant can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("dirname")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && arguments[0] instanceof ConstantReference) {
                        final String constantName = ((ConstantReference) arguments[0]).getName();
                        if (constantName != null && constantName.equals("__FILE__")) {
                            holder.registerProblem(
                                    reference,
                                    MessagesPresentationUtil.prefixWithEa(message),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new UseDirConstantFix()
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseDirConstantFix extends UseSuggestedReplacementFixer {
        private static final String title = "Replace by __DIR__";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseDirConstantFix() {
            super("__DIR__");
        }
    }
}
