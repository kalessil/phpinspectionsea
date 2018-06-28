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
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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
        callbacksPositions.put("call_user_func",       0);
        callbacksPositions.put("call_user_func_array", 0);
        callbacksPositions.put("array_filter",         1);
        callbacksPositions.put("array_map",            0);
        callbacksPositions.put("array_reduce",         1);
        callbacksPositions.put("array_walk",           1);
        callbacksPositions.put("array_walk_recursive", 1);
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
                if (functionName != null) {
                    if (functionName.equals("uniqid")) {
                        /* direct call */
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length < 2 && this.isFromRootNamespace(reference)) {
                            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR, new AddMissingParametersFix());
                        }
                    } else if (callbacksPositions.containsKey(functionName)) {
                        /* calling in callbacks */
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length >= 2) {
                            final int callbackPosition            = callbacksPositions.get(functionName);
                            final StringLiteralExpression literal = ExpressionSemanticUtil.resolveAsStringLiteral(arguments[callbackPosition]);
                            if (literal != null) {
                                final String raw      = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                                final String callback = raw.startsWith("\\") ? raw.substring(1) : raw;
                                if (callback.equals("uniqid")) {
                                    holder.registerProblem(arguments[callbackPosition], message, ProblemHighlightType.GENERIC_ERROR, new UseLambdaFix());
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseLambdaFix extends UseSuggestedReplacementFixer {
        private static final String title = "Add missing arguments";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseLambdaFix () {
            super("function($value){ return uniqid($value, true); }");
        }
    }

    private static final class AddMissingParametersFix implements LocalQuickFix {
        private static final String title = "Add missing arguments";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference && !project.isDisposed()) {
                final FunctionReference call = (FunctionReference) expression;
                final PsiElement[] params    = call.getParameters();

                /* override existing parameters */
                final FunctionReference replacement
                        = PhpPsiElementFactory.createFunctionReference(project, "uniqid('', true)");
                for (int index = 0; index < params.length; ++index) {
                    replacement.getParameters()[index].replace(params[index]);
                }

                /* replace parameters list */
                final PsiElement implant = replacement.getParameterList();
                final PsiElement socket  = call.getParameterList();
                if (implant != null && socket != null) {
                    socket.replace(implant);
                }
            }
        }
    }
}
