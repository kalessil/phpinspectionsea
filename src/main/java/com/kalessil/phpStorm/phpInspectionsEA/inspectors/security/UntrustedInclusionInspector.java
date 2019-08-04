package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Include;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UntrustedInclusionInspector extends BasePhpInspection {
    private static final String message = "This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "UntrustedInclusionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Untrusted files inclusion";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                final PsiElement argument = include.getArgument();
                final PsiElement file     = ExpressionSemanticUtil.resolveAsStringLiteral(argument);
                if (null != file) {
                    holder.registerProblem(include, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}