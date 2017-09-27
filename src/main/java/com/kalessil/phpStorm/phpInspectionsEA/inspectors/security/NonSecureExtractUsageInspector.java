package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureExtractUsageInspector extends BasePhpInspection {
    private static final String message = "Please provide second parameter to clearly state intended behaviour.";

    @NotNull
    public String getShortName() {
        return "NonSecureExtractUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String function     = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (1 == params.length && function != null && function.equals("extract")) {
                    /* ignore test classes */
                    final Function scope = ExpressionSemanticUtil.getScope(reference);
                    if (scope instanceof Method) {
                        final PhpClass clazz = ((Method) scope).getContainingClass();
                        if (null != clazz && FileSystemUtil.isTestClass(clazz)) {
                            return;
                        }
                    }

                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}
