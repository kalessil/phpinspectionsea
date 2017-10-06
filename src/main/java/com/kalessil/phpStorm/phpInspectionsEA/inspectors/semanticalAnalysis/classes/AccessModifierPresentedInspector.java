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
    // Inspection options.
    public boolean ANALYZE_INTERFACES = true;

    private static final String messagePattern = "'%s%' should be declared with access modifier.";

    @NotNull
    public String getShortName() {
        return "AccessModifierPresentedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                /* community request: interfaces have only public methods, what is default access levels */
                if (!ANALYZE_INTERFACES && clazz.isInterface()){
                    return;
                }

                /* inspect methods */
                for (final Method method : clazz.getOwnMethods()) {
                    final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                    if (methodName == null || !method.getAccess().isPublic()) {
                        continue;
                    }

                    final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(method, PhpModifierList.class);
                    if (modifiers != null && !modifiers.getText().toLowerCase().contains("public")) {
                        final String message = messagePattern.replace("%s%", method.getName());
                        holder.registerProblem(methodName, message, new TheLocalFix(modifiers));
                    }
                }

                /* inspect fields */
                for (final Field field : clazz.getOwnFields()) {
                    final PsiElement fieldName = NamedElementUtil.getNameIdentifier(field);
                    if (fieldName == null || field.isConstant() || !field.getModifier().isPublic()) {
                        continue;
                    }

                    final PhpModifierList modifiers = PsiTreeUtil.findChildOfType(field.getParent(), PhpModifierList.class);
                    if (modifiers != null && !modifiers.getText().toLowerCase().contains("public")) {
                        final String message = messagePattern.replace("%s%", field.getName());
                        holder.registerProblem(fieldName, message, new TheLocalFix(modifiers));
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create(
            (component) -> component.addCheckbox("Analyze interfaces", ANALYZE_INTERFACES, (isSelected) -> ANALYZE_INTERFACES = isSelected)
        );
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private SmartPsiElementPointer<PsiElement> modifiers;

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

        TheLocalFix(@NotNull PsiElement modifiers) {
            super();
            SmartPointerManager manager = SmartPointerManager.getInstance(modifiers.getProject());

            this.modifiers = manager.createSmartPsiElementPointer(modifiers);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement modifiers = this.modifiers.getElement();
            if (null != modifiers) {
                final String access  = modifiers.getText().replace("var", "").trim();
                final String pattern = "public %m% function x(){}"
                        .replace("%m% ", 0 == access.length() ? "%m%" : "%m% ")
                        .replace("%m%", access);

                final Method container       = PhpPsiElementFactory.createMethod(project, pattern);
                PhpModifierList newModifiers = PsiTreeUtil.findChildOfType(container, PhpModifierList.class);
                if (null != newModifiers) {
                    modifiers.replace(newModifiers);
                }
            }
        }
    }
}
