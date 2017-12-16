package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NonSecureUniqidUsageInspector extends BasePhpInspection {
    private static final String message = "Insufficient entropy, please provide both prefix and more entropy parameters.";

    final private static Map<String, Integer> callbacksPositions = new HashMap<>();
    static {
        callbacksPositions.put("call_user_func", 0);
        callbacksPositions.put("call_user_func_array", 0);
        callbacksPositions.put("array_filter", 1);
        callbacksPositions.put("array_map", 0);
        callbacksPositions.put("array_walk", 1);
        callbacksPositions.put("array_reduce", 1);
    }

    @NotNull
    public String getShortName() {
        return "NonSecureUniqidUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (null != functionName) {
                    final PsiElement[] params = reference.getParameters();
                    if (params.length < 2 && functionName.equals("uniqid")) {
                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new AddMissingParametersFix());
                    }

                    if (params.length >= 2 && callbacksPositions.containsKey(functionName)) {
                        final int neededIndex = callbacksPositions.get(functionName);
                        if (params[neededIndex] instanceof StringLiteralExpression) {
                            String callback = ((StringLiteralExpression) params[neededIndex]).getContents();
                            if (callback.startsWith("\\")) {
                                callback = callback.substring(1);
                            }
                            if (callback.equals("uniqid")) {
                                holder.registerProblem(params[neededIndex], message, ProblemHighlightType.GENERIC_ERROR, new UseLambdaFix());
                            }
                        }
                    }
                }
            }
        };
    }

    private static class UseLambdaFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Add missing arguments";
        }

        UseLambdaFix () {
            super("function($value){ return uniqid($value, true); }");
        }
    }

    private static class AddMissingParametersFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Add missing arguments";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                final FunctionReference call = (FunctionReference) expression;
                final PsiElement[] params    = call.getParameters();

                /* override existing parameters */
                final FunctionReference replacement
                        = PhpPsiElementFactory.createFunctionReference(project, "uniqid('', true)");
                for (int index = 0; index < params.length; ++index) {
                    replacement.getParameters()[index].replace(params[index]);
                }

                /* replace parameters list */
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
            }
        }
    }
}
