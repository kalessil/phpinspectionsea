package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrtotimeUsageInspector extends BasePhpInspection {
    private static final String messageUseTime  = "You shall use time() function instead (2x faster)";
    private static final String messageDropTime = "'time()' is a default valued already, safely drop it";

    @NotNull
    public String getShortName() {
        return "StrtotimeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if (
                    params.length == 0 || params.length > 2 ||
                    StringUtil.isEmpty(functionName) || !functionName.equals("strtotime")
                ) {
                    return;
                }

                /* handle case: strtotime("now") -> time() */
                if (params.length == 1) {
                    if (params[0] instanceof StringLiteralExpression) {
                        final StringLiteralExpression pattern = (StringLiteralExpression) params[0];
                        if (pattern.getContents().equalsIgnoreCase("now")) {
                            holder.registerProblem(reference, messageUseTime, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UseTimeFunctionLocalFix());
                        }
                    }
                    return;
                }

                /* handle case: strtotime(..., time()) -> date(...) */
                if (params.length == 2) {
                    if (params[1] instanceof FunctionReferenceImpl) {
                        final FunctionReferenceImpl call = (FunctionReferenceImpl) params[1];
                        final String callName            = call.getName();
                        if (!StringUtil.isEmpty(callName) && callName.equals("time")) {
                            holder.registerProblem(params[1], messageDropTime, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                        }
                    }
                    // return;
                }
            }
        };
    }

    private static class UseTimeFunctionLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use time()";
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
                expression.replace(PhpPsiElementFactory.createFunctionReference(project, "time()"));
            }
        }
    }
}
