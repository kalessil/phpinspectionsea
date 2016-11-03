package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpContinueImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SwitchContinuationInLoopInspector extends BasePhpInspection {
    private static final String strProblemDescription = "In PHP continue inside switch behaves as break. Use 'continue 2;' for continuation of an external loop.";

    @NotNull
    public String getShortName() {
        return "SwitchContinuationInLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpContinue(PhpContinue continueStatement) {
                /* check if continue already defined with desired level */
                if (null != continueStatement.getFirstPsiChild()) {
                    return;
                }

                boolean isSwitch = false;
                PsiElement objParent = continueStatement.getParent();
                while (null != objParent) {
                    /* reached file or callable */
                    if (objParent instanceof PhpFile || objParent instanceof Function) {
                        return;
                    }

                    /* check if shall operate on loop-switch-continue analysis */
                    if (!isSwitch && objParent instanceof PhpSwitch) {
                        isSwitch = true;
                    }

                    /* when met a loop, complete analysis */
                    if (objParent instanceof For || objParent instanceof ForeachStatement || objParent instanceof While) {
                        if (isSwitch) {
                            final UseContinue2LocalFix fixer = new UseContinue2LocalFix();
                            holder.registerProblem(continueStatement, strProblemDescription, ProblemHighlightType.GENERIC_ERROR, fixer);
                        }

                        return;
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }

    private static class UseContinue2LocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Use 'continue 2;'";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof PhpContinue) {
                //noinspection ConstantConditions I' sure that NPE will not happen as we have hardcoded pattern
                expression.replace(PhpPsiElementFactory.createFromText(project, PhpContinueImpl.class, "continue 2;"));
            }
        }
    }
}