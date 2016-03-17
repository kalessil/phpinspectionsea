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
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class MktimeUsageInspector extends BasePhpInspection {
    private static final String strProblemUseTime             = "You shall use time() function instead (current usage produces a runtime warning)";
    private static final String strProblemParameterDeprecated = "Parameter 'is_dst' is deprecated and removed in PHP 7";

    @NotNull
    public String getShortName() {
        return "MktimeUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check parameters amount and name */
                final String strFunctionName = reference.getName();
                final PsiElement[] params    = reference.getParameters();
                final int parametersCount    = params.length;
                if (
                    StringUtil.isEmpty(strFunctionName) ||
                    !(
                        (0 == parametersCount ||  7 == parametersCount) &&
                        (strFunctionName.equals("mktime") || strFunctionName.equals("gmmktime"))
                    )
                ) {
                    return;
                }

                /* report the issue */
                if (0 == parametersCount) {
                    final UseTimeFunctionLocalFix fixer = new UseTimeFunctionLocalFix();
                    holder.registerProblem(reference, strProblemUseTime, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixer);
                } else {
                    holder.registerProblem(params[6], strProblemParameterDeprecated, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
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