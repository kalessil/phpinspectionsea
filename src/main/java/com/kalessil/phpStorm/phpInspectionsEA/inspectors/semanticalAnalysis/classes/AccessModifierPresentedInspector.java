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
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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
    @Override
    public String getShortName() {
        return "AccessModifierPresentedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Access modifiers shall be defined";
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
                            holder.registerProblem(
                                    methodName,
                                    MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, method.getName())),
                                    new MemberVisibilityFix(holder.getProject(), modifiers)
                            );
                        }
                    }
                }

                /* inspect fields */
                final boolean checkConstantVisibility = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP710);
                for (final Field field : clazz.getOwnFields()) {
                    final PsiElement fieldName = NamedElementUtil.getNameIdentifier(field);
                    if (fieldName != null && field.getModifier().isPublic()) {
                        if (field.isConstant()) {
                            /* {const} inspection should be skipped if PHP version < 7.1.0. */
                            /* {const}.isPublic() always returns true, even if visibility is not declared */
                            if (ANALYZE_CONSTANTS && checkConstantVisibility && field.getPrevPsiSibling() == null) {
                                holder.registerProblem(
                                        fieldName,
                                        MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, field.getName())),
                                        new ConstantVisibilityFix(holder.getProject(), field)
                                );
                            }
                        } else {
                            final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(field.getParent(), PhpModifierList.class);
                            if (modifiers != null && !modifiers.getText().toLowerCase().contains("public")) {
                                holder.registerProblem(
                                        fieldName,
                                        MessagesPresentationUtil.prefixWithEa(String.format(messagePattern, field.getName())),
                                        new MemberVisibilityFix(holder.getProject(), modifiers)
                                );
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

    private static final class MemberVisibilityFix implements LocalQuickFix {
        private static final String title = "Declare the member public";

        final private SmartPsiElementPointer<PhpModifierList> modifiersReference;

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

        MemberVisibilityFix(@NotNull Project project, @NotNull PhpModifierList modifiers) {
            super();

            this.modifiersReference = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(modifiers);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PhpModifierList modifiers = this.modifiersReference.getElement();
            if (modifiers != null && !project.isDisposed()) {
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

    private static final class ConstantVisibilityFix implements LocalQuickFix {
        private static final String title = "Declare the constant public";

        private final SmartPsiElementPointer<Field> constFieldReference;

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

        ConstantVisibilityFix(@NotNull Project project, @NotNull Field constField) {
            super();

            this.constFieldReference = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(constField);
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
