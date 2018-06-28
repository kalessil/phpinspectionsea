package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
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
                final PhpClass clazz      = method.getContainingClass();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == clazz || null == nameNode || clazz.isTrait() || clazz.isInterface()) {
                    return;
                }

                final String className = clazz.getName();
                if (className.equals(method.getName()) && null == clazz.findOwnMethodByName("__construct")) {
                    final String message = messagePattern.replace("%s%", className);
                    holder.registerProblem(nameNode, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Rename to __construct";

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
            final PsiElement reportingTarget = descriptor.getPsiElement();
            final PsiElement expression      = reportingTarget.getParent();
            if (expression instanceof Method) {
                PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "__construct");
                reportingTarget.replace(replacement);
            }
        }
    }
}
