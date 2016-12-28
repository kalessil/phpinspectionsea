package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AccessModifierPresentedInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
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
            public void visitPhpClass(PhpClass clazz) {
                /* community request: interfaces have only public methods, what is default access levels */
                if (!ANALYZE_INTERFACES && clazz.isInterface()){
                    return;
                }

                /* inspect methods */
                for (Method method : clazz.getOwnMethods()) {
                    if (!method.getAccess().isPublic()) {
                        continue;
                    }

                    /* find modifiers list */
                    String modifiers = null;
                    for (PsiElement child : method.getChildren()) {
                        if (child instanceof PhpModifierList) {
                            modifiers = child.getText();
                            break;
                        }
                    }

                    final PsiElement methodNameNode = method.getNameIdentifier();
                    if (null != modifiers && null != methodNameNode && !(methodNameNode instanceof PsiErrorElement)) {
                        /* scan modifiers defined */
                        /* TODO: use field.getModifier() */
                        final boolean hasAccessModifiers =
                                modifiers.contains("public") ||
                                modifiers.contains("protected") ||
                                modifiers.contains("private");

                        /* scan modifiers defined */
                        if (!hasAccessModifiers) {
                            final String message = messagePattern.replace("%s%", method.getName());
                            holder.registerProblem(methodNameNode, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }

                /* inspect fields */
                for (Field field : clazz.getOwnFields()) {
                    if (field.isConstant()) {
                        continue;
                    }

                    /* find modifiers list */
                    String modifiers = null;
                    for (PsiElement child : field.getParent().getChildren()) {
                        if (child instanceof PhpModifierList) {
                            modifiers = child.getText();
                            break;
                        }
                    }

                    if (null != modifiers && null != field.getNameIdentifier()) {
                        /* scan modifiers defined */
                        /* TODO: use field.getModifier() */
                        final boolean hasAccessModifiers =
                                modifiers.contains("public") ||
                                modifiers.contains("protected") ||
                                modifiers.contains("private");

                        /* report issues */
                        if (!hasAccessModifiers) {
                            final String message = messagePattern.replace("%s%", field.getName());
                            holder.registerProblem(field.getNameIdentifier(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new AccessModifierPresentedInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox analyzeInterfaces;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            analyzeInterfaces = new JCheckBox("Analyze interfaces", ANALYZE_INTERFACES);
            analyzeInterfaces.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    ANALYZE_INTERFACES = analyzeInterfaces.isSelected();
                }
            });
            optionsPanel.add(analyzeInterfaces, "wrap");
        }

        JPanel getComponent() {
            return optionsPanel;
        }
    }
}
