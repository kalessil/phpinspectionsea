package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
    @Override
    public String getShortName() {
        return "NonSecureExtractUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Insecure 'extract(...)' usage (Variable extract Vulnerability)";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("extract")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1 && !this.isTestContext(reference)) {
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }
}
