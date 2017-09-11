package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

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
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    final String methodName      = reference.getName();
                    final PsiElement[] arguments = reference.getParameters();
                    if (methodName != null && arguments.length == 4 && methodName.equals("setTime")) {
                        final PsiElement resolved = reference.resolve();
                        if (resolved instanceof Method && ((Method) resolved).getFQN().equals("\\DateTime.setTime")) {
                            holder.registerProblem(arguments[3], message);
                        }
                    }
                }
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.compareTo(PhpLanguageLevel.PHP710) < 0) {
                    final String functionName    = reference.getName();
                    final PsiElement[] arguments = reference.getParameters();
                    if (functionName != null && arguments.length == 5 && functionName.equals("date_time_set")) {
                        final PsiElement resolved = reference.resolve();
                        if (resolved instanceof Function && ((Function) resolved).getFQN().equals("\\date_time_set")) {
                            holder.registerProblem(arguments[4], message);
                        }
                    }
                }
            }
        };
    }
}
