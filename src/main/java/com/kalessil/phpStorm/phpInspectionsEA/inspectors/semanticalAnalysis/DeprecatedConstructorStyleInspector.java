package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class DeprecatedConstructorStyleInspector extends BasePhpInspection {
    private static final String messagePattern = "%s% has a deprecated constructor.";

    @NotNull
    public String getShortName() {
        return "DeprecatedConstructorStyleInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                final PhpClass clazz    = method.getContainingClass();
                final String methodName = method.getName();
                if (
                    null == clazz || clazz.isTrait() || clazz.isInterface() ||
                    StringUtil.isEmpty(methodName) || null == method.getNameIdentifier()
                ) {
                    return;
                }

                final String className = clazz.getName();
                if (methodName.equals(className) && null == clazz.findOwnMethodByName("__construct")) {
                    final String message = messagePattern.replace("%s%", className);
                    holder.registerProblem(method.getNameIdentifier(), message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Rename to __construct";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement reportingTarget = descriptor.getPsiElement();
            final PsiElement expression      = reportingTarget.getParent();
            if (expression instanceof Method) {
                PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "__construct");
                //noinspection ConstantConditions I'm sure NPE will not happen here
                reportingTarget.replace(replacement);
            }
        }
    }
}
