package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpUnset;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequentialUnSetCallsInspector extends BasePhpInspection {
    private static final String message = "Can be safely replaced with 'unset(..., ...[, ...])' construction";

    @NotNull
    public String getShortName() {
        return "SequentialUnSetCallsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpUnset(PhpUnset unsetStatement) {
                PsiElement previous = unsetStatement.getPrevPsiSibling();
                while (previous instanceof PhpDocComment) {
                    previous = ((PhpDocComment) previous).getPrevPsiSibling();
                }

                if (previous instanceof PhpUnset) {
                    holder.registerProblem(unsetStatement, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(unsetStatement));
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PhpUnset> unset;

        @NotNull
        @Override
        public String getName() {
            return "Merge unset statements";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        TheLocalFix(@NotNull PhpUnset unset) {
            super();
            SmartPointerManager manager =  SmartPointerManager.getInstance(unset.getProject());

            this.unset = manager.createSmartPsiElementPointer(unset);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PhpUnset unset = this.unset.getElement();
            if (null != unset) {
                synchronized (unset.getContainingFile()) {
                    /* find preceding unset-statement */
                    PsiElement previous = unset.getPrevPsiSibling();
                    while (previous instanceof PhpDocComment) {
                        previous = ((PhpDocComment) previous).getPrevPsiSibling();
                    }
                    if (!(previous instanceof PhpUnset)) {
                        return;
                    }

                    /* collect all parameters */
                    final List<PsiElement> params     = new ArrayList<>();
                    Collections.addAll(params, ((PhpUnset) previous).getArguments());
                    Collections.addAll(params, unset.getArguments());

                    /* generate target expression */
                    final List<String> paramsAsString = new ArrayList<>();
                    for (PsiElement param : params) {
                        paramsAsString.add(param.getText());
                    }
                    final String pattern = "unset(%p%);".replace("%p%", String.join(", ", paramsAsString));
                    paramsAsString.clear();
                    params.clear();

                    /* apply refactoring */
                    final PhpUnset merged = PhpPsiElementFactory.createFromText(project, PhpUnset.class, pattern);
                    if (null != merged) {
                        previous.replace(merged);
                        unset.delete();
                    }
                }
            }
        }
    }
}