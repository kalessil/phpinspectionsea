package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Include;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.ControlStatementImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class UsingInclusionOnceReturnValueInspector extends BasePhpInspection {
    private static final String message = "Only first call returns proper result. Repetitive calls returning 'true'.";

    @NotNull
    public String getShortName() {
        return "UsingInclusionOnceReturnValueInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpInclude(Include include) {
                final PsiElement parent = include.getParent();
                if (parent instanceof ControlStatementImpl || !(parent instanceof StatementImpl)) {
                    if (null == include.getArgument() || !include.getFirstChild().getText().endsWith("_once")) {
                        return;
                    }

                    holder.registerProblem(include, message, ProblemHighlightType.GENERIC_ERROR, new TheLocalFix());
                }
            }
        };
    }

    static private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use include/require instead";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement target = descriptor.getPsiElement();
            if (target instanceof Include) {
                boolean isInclude = PhpTokenTypes.kwINCLUDE_ONCE == target.getFirstChild().getNode().getElementType();
                String pattern    = isInclude ? "include ''" : "require ''";

                PhpPsiElement replacement = PhpPsiElementFactory.createPhpPsiFromText(project, Include.class, pattern);
                //noinspection ConstantConditions pattern hardcoded and arg is checked - safe
                ((Include) replacement).getArgument().replace(((Include) target).getArgument());
                target.replace(replacement);
            }
        }
    }
}
