package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProtectedWithFinalClassInspector extends BasePhpInspection {
    private static final String message = "Protected modifier could be replaced by private.";

    @NotNull
    public String getShortName() {
        return "ProtectedWithFinalClassInspector";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            private void checkElement(final PhpElementWithModifier element) {
                if (!element.getModifier().isProtected()) {
                    return;
                }

                final PhpClass elementClass = ((PhpClassMember) element).getContainingClass();

                assert elementClass != null;

                if (!elementClass.isFinal()) {
                    return;
                }

                if (isOverridedFromProtected((PsiElement) element, elementClass)) {
                    return;
                }

                final PsiElement elementProtectedModifier = getProtectedModifier((PsiElement) element);
                if (elementProtectedModifier != null) {
                    problemsHolder.registerProblem(elementProtectedModifier, message, ProblemHighlightType.WEAK_WARNING,
                                                   new TheLocalFix(elementProtectedModifier));
                }
            }

            private boolean isOverridedFromProtected(final PsiElement element, final PhpClass elementClass) {
                final String  elementName = ((NavigationItem) element).getName();
                final boolean isField     = element instanceof Field;
                final boolean isConstant  = isField && ((Field) element).isConstant();

                for (final PhpClass superClass : elementClass.getSupers()) {
                    final PsiElement compatibleElement = isField
                                                         ? superClass.findFieldByName(elementName, isConstant)
                                                         : superClass.findMethodByName(elementName);

                    if (compatibleElement != null) {
                        return getProtectedModifier(compatibleElement) != null;
                    }
                }

                return false;
            }

            @Nullable
            private PsiElement getProtectedModifier(final PsiElement element) {
                final PsiElement       elementModifierScope = (element instanceof Field) ? element.getParent() : element;
                final PhpModifierList  elementModifierList  = PsiTreeUtil.findChildOfType(elementModifierScope, PhpModifierList.class);
                final LeafPsiElement[] elementModifiers     = PsiTreeUtil.getChildrenOfType(elementModifierList, LeafPsiElement.class);

                assert elementModifiers != null;

                for (final LeafPsiElement elementModifier : elementModifiers) {
                    if ("protected".equalsIgnoreCase(elementModifier.getText())) {
                        return elementModifier;
                    }
                }

                return null;
            }

            @Override
            public void visitPhpField(final Field field) {
                // Note: it does the work for properties and constants.
                checkElement(field);
            }

            @Override
            public void visitPhpMethod(final Method method) {
                checkElement(method);
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> modifier;

        TheLocalFix(@NotNull final PsiElement modifierElement) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(modifierElement.getProject());

            modifier = manager.createSmartPsiElementPointer(modifierElement);
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare private";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement modifierElement     = modifier.getElement();
            final PsiElement modifierReplacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "private");

            assert modifierElement != null;
            assert modifierReplacement != null;

            modifierElement.replace(modifierReplacement);
        }
    }
}
