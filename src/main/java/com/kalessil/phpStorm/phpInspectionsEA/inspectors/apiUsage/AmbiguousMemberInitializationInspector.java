package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class AmbiguousMemberInitializationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Null assignment can be safely removed. Define null in annotations if it's important";

    @NotNull
    public String getShortName() {
        return "AmbiguousMemberInitializationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field field) {
                if (field.isConstant()) {
                    return;
                }

                final PsiElement objDefaultValue = field.getDefaultValue();
                if (objDefaultValue instanceof ConstantReference && PhpType.NULL == ((ConstantReference) objDefaultValue).getType()) {
                    holder.registerProblem(objDefaultValue, strProblemDescription, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove null assignment";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final ConstantReference nullValue = (ConstantReference) descriptor.getPsiElement();
            final Field field                 = (Field) nullValue.getParent();

            final PsiElement fieldName = field.getNameIdentifier();
            if (null != fieldName) {
                field.deleteChildRange(fieldName.getNextSibling(), nullValue);
            }
        }
    }
}

