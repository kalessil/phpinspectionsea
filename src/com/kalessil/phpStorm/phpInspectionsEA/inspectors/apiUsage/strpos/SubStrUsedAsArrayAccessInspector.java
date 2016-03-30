package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

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
import com.jetbrains.php.lang.psi.elements.impl.ArrayAccessExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrUsedAsArrayAccessInspector extends BasePhpInspection {
    private static final String messagePattern = "'%c%[%i%]' might be used instead (invalid index accesses will popup)";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function */
                final String function     = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (3 != params.length || StringUtil.isEmpty(function) || !function.equals("substr")) {
                    return;
                }

                if (params[2] instanceof PhpExpressionImpl && params[2].getText().replaceAll("\\s+","").equals("1")) {
                    final String message = messagePattern
                        .replace("%c%", params[0].getText())
                        .replace("%i%", params[1].getText())
                    ;
                    holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use array access";
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
                final PsiElement[] params = ((FunctionReference) expression).getParameters();
                final PsiElement pattern  = PhpPsiElementFactory.createFromText(project, ArrayAccessExpressionImpl.class, "$x[$y]");

                final ArrayAccessExpressionImpl replacement = (ArrayAccessExpressionImpl) pattern;
                //noinspection ConstantConditions I'm sure that NPE will not happen - pattern is hardcoded
                replacement.getValue().replace(params[0].copy());
                //noinspection ConstantConditions I'm sure that NPE will not happen - pattern is hardcoded
                replacement.getIndex().getValue().replace(params[1].copy());

                expression.replace(replacement);
            }
        }
    }
}
