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
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class DirnameCallOnFileConstantInspector extends BasePhpInspection {
    private final LocalQuickFix fixQuick              = new TheLocalFix();
    private static final String strProblemDescription = "__DIR__ should be used instead";

    @NotNull
    public String getShortName() {
        return "dirnameCallOnFileConstantInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check requirements */
                final PsiElement[] params = reference.getParameters();
                final String name         = reference.getName();
                if (1 != params.length || StringUtil.isEmpty(name) || !name.equals("dirname")) {
                    return;
                }
                final PsiElement firstParameter = params[0];
                if (!(firstParameter instanceof ConstantReference)) {
                    return;
                }

                /* inspect given construct */
                final String constant = ((ConstantReference) firstParameter).getName();
                if (!StringUtil.isEmpty(constant) && constant.equals("__FILE__")) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.LIKE_DEPRECATED, fixQuick);
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Replace by __DIR__";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement target = descriptor.getPsiElement();
            if (target instanceof FunctionReference) {
                target.replace(PhpPsiElementFactory.createConstantReference(project, "__DIR__"));
            }
        }
    }
}
