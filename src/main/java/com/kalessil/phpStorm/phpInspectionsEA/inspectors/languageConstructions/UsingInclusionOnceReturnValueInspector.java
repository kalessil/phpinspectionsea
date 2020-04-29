package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ControlStatement;
import com.jetbrains.php.lang.psi.elements.Include;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class UsingInclusionOnceReturnValueInspector extends BasePhpInspection {
    private static final String message = "Only the first call returns the proper/expected result. Subsequent calls will return 'true'.";

    @NotNull
    @Override
    public String getShortName() {
        return "UsingInclusionOnceReturnValueInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Suspicious usage of include_once/require_once return value";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                final PsiElement parent = include.getParent();
                if (parent instanceof ControlStatement || !OpenapiTypesUtil.isStatementImpl(parent)) {
                    if (include.getArgument() != null && include.getFirstChild().getText().endsWith("_once")) {
                        holder.registerProblem(
                                include,
                                MessagesPresentationUtil.prefixWithEa(message),
                                new TheLocalFix()
                        );
                    }
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use include/require instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof Include && !project.isDisposed()) {
                final boolean isInclude   = OpenapiTypesUtil.is(target.getFirstChild(), PhpTokenTypes.kwINCLUDE_ONCE);
                final String pattern      = isInclude ? "include ''" : "require ''";
                final Include replacement = PhpPsiElementFactory.createPhpPsiFromText(project, Include.class, pattern);
                replacement.getArgument().replace(((Include) target).getArgument());
                target.replace(replacement);
            }
        }
    }
}
