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
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpContinue;
import com.jetbrains.php.lang.psi.elements.PhpSwitch;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

public class SwitchContinuationInLoopInspector extends BasePhpInspection {
    private static final String message = "In PHP, 'continue' inside a 'switch' behaves as 'break'. Use 'continue 2;' to continue the external loop.";

    @NotNull
    @Override
    public String getShortName() {
        return "SwitchContinuationInLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Continue misbehaviour in switch";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpContinue(@NotNull PhpContinue continueStatement) {
                /* check if continue already defined with desired level */
                if (null != continueStatement.getFirstPsiChild()) {
                    return;
                }

                boolean isSwitch     = false;
                PsiElement objParent = continueStatement.getParent();
                while (null != objParent) {
                    /* reached file or callable */
                    if (objParent instanceof PhpFile || objParent instanceof Function) {
                        return;
                    }

                    /* check if should operate on loop-switch-continue analysis */
                    if (!isSwitch && objParent instanceof PhpSwitch) {
                        isSwitch = true;
                    }

                    /* when met a loop, complete analysis */
                    if (OpenapiTypesUtil.isLoop(objParent)) {
                        if (isSwitch) {
                            holder.registerProblem(
                                    continueStatement,
                                    MessagesPresentationUtil.prefixWithEa(message),
                                    ProblemHighlightType.GENERIC_ERROR,
                                    new UseContinue2LocalFix()
                            );
                        }

                        return;
                    }

                    objParent = objParent.getParent();
                }
            }
        };
    }

    private static final class UseContinue2LocalFix implements LocalQuickFix {
        private static final String title = "Use 'continue 2;'";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof PhpContinue && !project.isDisposed()) {
                expression.replace(PhpPsiElementFactory.createFromText(project, PhpContinue.class, "continue 2;"));
            }
        }
    }
}