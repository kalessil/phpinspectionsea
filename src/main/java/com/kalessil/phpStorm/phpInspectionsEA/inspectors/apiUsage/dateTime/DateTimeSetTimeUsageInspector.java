package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class DateTimeSetTimeUsageInspector extends BasePhpInspection {
    private static final String message = "The call will return false ('microseconds' parameter is available in PHP 7.1+).";

    @NotNull
    public String getShortName() {
        return "DateTimeSetTimeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    final String methodName = reference.getName();
                    if (methodName != null && methodName.equals("setTime")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 4 && arguments[3] instanceof PhpPsiElement) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof Method && ((Method) resolved).getFQN().equals("\\DateTime.setTime")) {
                                holder.registerProblem(arguments[3], message);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.isContainingFileSkipped(reference)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    final String functionName = reference.getName();
                    if (functionName != null && functionName.equals("date_time_set")) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 5 && this.isFromRootNamespace(reference)) {
                            holder.registerProblem(arguments[4], message);
                        }
                    }
                }
            }
        };
    }
}
