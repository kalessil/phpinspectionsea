package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

public class AccessModifierPresentedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be declared with access modifier.";

    // Inspection options.
    public boolean ANALYZE_INTERFACES = true;

    @NotNull
    public String getShortName() {
        return "AccessModifierPresentedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(final PhpClass clazz) {
                /* community request: interfaces have only public methods, what is default access levels */
                if (!ANALYZE_INTERFACES && clazz.isInterface()) {
                    return;
                }

                /* inspect methods */
                for (final Method method : clazz.getOwnMethods()) {
                    final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);

                    if ((methodName == null) || !method.getAccess().isPublic()) {
                        continue;
                    }

                    final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);

                    if ((modifiers != null)) {
                        if (!modifiers.getText().contains("public")) {
                            final String message = String.format(messagePattern, method.getName());
                            holder.registerProblem(methodName, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix(modifiers));
                        }
                    }
                }

                /* inspect fields */
                for (final Field field : clazz.getOwnFields()) {
                    final PsiElement fieldName = NamedElementUtil.getNameIdentifier(field);

                    if ((fieldName == null) || field.isConstant() || !field.getModifier().isPublic()) {
                        continue;
                    }

                    final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(field.getParent(), PhpModifierList.class);

                    if ((modifiers != null) && !modifiers.getText().contains("public")) {
                        final String message = String.format(messagePattern, field.getName());
                        holder.registerProblem(fieldName, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix(modifiers));
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Analyze interfaces", ANALYZE_INTERFACES, (isSelected) -> ANALYZE_INTERFACES = isSelected);
        });
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PhpModifierList> modifiersReference;

        TheLocalFix(@NotNull final PhpModifierList modifiers) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(modifiers.getProject());

            modifiersReference = manager.createSmartPsiElementPointer(modifiers);
        }

        @NotNull
        @Override
        public String getName() {
            return "Declare public";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PhpModifierList modifiers = modifiersReference.getElement();

            if (modifiers != null) {
                final String modifierFinal    = modifiers.hasFinal() ? "final " : "";
                final String modifierAbstract = modifiers.hasAbstract() ? "abstract " : "";
                final String modifierStatic   = modifiers.hasStatic() ? " static" : "";
                final String pattern          = String.format("%s%spublic%s function x(){}", modifierFinal, modifierAbstract, modifierStatic);

                final Method          container    = PhpPsiElementFactory.createMethod(project, pattern);
                final PhpModifierList newModifiers = PsiTreeUtil.findChildOfType(container, PhpModifierList.class);

                if (newModifiers != null) {
                    modifiers.replace(newModifiers);
                }
            }
        }
    }
}
