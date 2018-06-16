package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class AccessModifierPresentedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' should be declared with access modifier.";

    // Inspection options.
    public boolean ANALYZE_INTERFACES = true;
    public boolean ANALYZE_CONSTANTS  = true;

    @NotNull
    public String getShortName() {
        return "AccessModifierPresentedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                /* community request: interfaces have only public methods, what is default access levels */
                if (!ANALYZE_INTERFACES && clazz.isInterface()) {
                    return;
                }

                /* inspect methods */
                for (final Method method : clazz.getOwnMethods()) {
                    final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                    if (methodName != null && method.getAccess().isPublic()) {
                        final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);
                        if (modifiers != null && !modifiers.getText().toLowerCase().contains("public")) {
                            final String message = String.format(messagePattern, method.getName());
                            holder.registerProblem(methodName, message, new MemberVisibilityFix(modifiers));
                        }
                    }
                }

                /* inspect fields */
                final PhpLanguageLevel phpVersion     = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                final boolean checkConstantVisibility = phpVersion.compareTo(PhpLanguageLevel.PHP710) >= 0;
                for (final Field field : clazz.getOwnFields()) {
                    final PsiElement fieldName = NamedElementUtil.getNameIdentifier(field);
                    if (fieldName != null && field.getModifier().isPublic()) {
                        if (field.isConstant()) {
                            /* {const} inspection should be skipped if PHP version < 7.1.0. */
                            /* {const}.isPublic() always returns true, even if visibility is not declared */
                            if (ANALYZE_CONSTANTS && checkConstantVisibility && field.getPrevPsiSibling() == null) {
                                final String message = String.format(messagePattern, field.getName());
                                holder.registerProblem(fieldName, message, new ConstantVisibilityFix(field));
                            }
                        } else {
                            final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(field.getParent(), PhpModifierList.class);
                            if (modifiers != null && !modifiers.getText().toLowerCase().contains("public")) {
                                final String message = String.format(messagePattern, field.getName());
                                holder.registerProblem(fieldName, message, new MemberVisibilityFix(modifiers));
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Analyze interfaces", ANALYZE_INTERFACES, (isSelected) -> ANALYZE_INTERFACES = isSelected);
            component.addCheckbox("Analyze constants", ANALYZE_CONSTANTS, (isSelected) -> ANALYZE_CONSTANTS = isSelected);
        });
    }

    private static class MemberVisibilityFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PhpModifierList> modifiersReference;

        @NotNull
        @Override
        public String getName() {
            return "Declare public";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName() + " (member)";
        }

        MemberVisibilityFix(@NotNull PhpModifierList modifiers) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(modifiers.getProject());
            this.modifiersReference           = manager.createSmartPsiElementPointer(modifiers);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PhpModifierList modifiers = this.modifiersReference.getElement();
            if (modifiers != null) {
                final String modifierFinal    = modifiers.hasFinal() ? "final " : "";
                final String modifierAbstract = modifiers.hasAbstract() ? "abstract " : "";
                final String modifierStatic   = modifiers.hasStatic() ? " static" : "";
                final String pattern          = String.format("%s%spublic%s function x(){}", modifierFinal, modifierAbstract, modifierStatic);

                final Method donor            = PhpPsiElementFactory.createMethod(project, pattern);
                final PhpModifierList implant = PsiTreeUtil.findChildOfType(donor, PhpModifierList.class);
                if (implant != null) {
                    modifiers.replace(implant);
                }
            }
        }
    }

    private static class ConstantVisibilityFix implements LocalQuickFix {
        private final SmartPsiElementPointer<Field> constFieldReference;

        @NotNull
        @Override
        public String getName() {
            return "Declare public";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName() + " (constant)";
        }

        ConstantVisibilityFix(@NotNull Field constField) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(constField.getProject());
            this.constFieldReference          = manager.createSmartPsiElementPointer(constField);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final Field constField = this.constFieldReference.getElement();
            if (constField != null && !project.isDisposed()) {
                final Method donor            = PhpPsiElementFactory.createMethod(project, "public function x(){}");
                final PhpModifierList implant = PsiTreeUtil.findChildOfType(donor, PhpModifierList.class);
                if (implant != null) {
                    final PsiElement constKeyword = constField.getParent().getFirstChild();
                    constKeyword.getParent().addBefore(implant, constKeyword);
                }
            }
        }
    }
}
