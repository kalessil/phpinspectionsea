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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

public class DeprecatedConstructorStyleInspector extends BasePhpInspection {
    private static final String messagePattern = "%s% has a deprecated constructor.";

    @NotNull
    @Override
    public String getShortName() {
        return "DeprecatedConstructorStyleInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Deprecated constructor style";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpClass clazz      = method.getContainingClass();
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                if (null == clazz || null == nameNode || clazz.isTrait() || clazz.isInterface()) {
                    return;
                }

                final String className = clazz.getName();
                if (className.equals(method.getName()) && null == clazz.findOwnMethodByName("__construct")) {
                    holder.registerProblem(
                            nameNode,
                            ReportingUtil.wrapReportedMessage(messagePattern.replace("%s%", className)),
                            ProblemHighlightType.LIKE_DEPRECATED,
                            new TheLocalFix()
                    );
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
            if (expression instanceof Method && !project.isDisposed()) {
                PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "__construct");
                reportingTarget.replace(replacement);
            }
        }
    }
}
