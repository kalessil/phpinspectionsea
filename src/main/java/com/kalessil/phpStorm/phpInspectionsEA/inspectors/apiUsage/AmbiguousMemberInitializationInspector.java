package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

public class AmbiguousMemberInitializationInspector extends BasePhpInspection {
    private static final String message = "Null assignment can be safely removed. Define null in annotations if it's important.";

    @NotNull
    public String getShortName() {
        return "AmbiguousMemberInitializationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpField(Field field) {
                final PsiElement defaultValue = field.getDefaultValue();
                if (!field.isConstant() && PhpLanguageUtil.isNull(defaultValue)) {
                    /* if parent has non-private field, check its' defaults (terminate if non-null defaults) */
                    final PhpClass clazz       = field.getContainingClass();
                    final PhpClass parentClazz = null == clazz ? null : clazz.getSuperClass();
                    final Field parentField    = null == parentClazz ? null : parentClazz.findFieldByName(field.getName(), false);
                    if (null != parentField && !parentField.isConstant() && !parentField.getModifier().isPrivate()) {
                        final PsiElement parentDefaultValue = parentField.getDefaultValue();
                        if (parentDefaultValue instanceof PhpPsiElement && !PhpLanguageUtil.isNull(parentDefaultValue)) {
                            return;
                        }
                    }

                    /* fire a warning */
                    holder.registerProblem(defaultValue, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TheLocalFix());
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

            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(field);
            if (null != nameNode) {
                field.deleteChildRange(nameNode.getNextSibling(), nullValue);
            }
        }
    }
}

