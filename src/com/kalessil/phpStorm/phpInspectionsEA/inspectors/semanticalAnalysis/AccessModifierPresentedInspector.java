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

    private static final String strProblemDescription = "%s% should be declared with access modifier.";

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

                /** inspect methods */
                for (Method objMethod : clazz.getOwnMethods()) {
                    if (objMethod.getAccess().isPublic()) {
                        /** find modifiers list */
                        String strModifiers = null;
                        for (PsiElement objChild : objMethod.getChildren()) {
                            if (objChild instanceof PhpModifierList) {
                                strModifiers = objChild.getText();
                                break;
                            }
                        }

                        final PsiElement methodNameNode = objMethod.getNameIdentifier();
                        if (null != strModifiers && null != methodNameNode && !(methodNameNode instanceof PsiErrorElement)) {
                            /** scan modifiers defined */
                            /** TODO: re-evaluate if JB completed modifiers list construction */
                            boolean hasAccessModifiers =
                                    strModifiers.contains("public") ||
                                    strModifiers.contains("protected") ||
                                    strModifiers.contains("private");

                            /** scan modifiers defined */
                            if (!hasAccessModifiers) {
                                String strWarning = strProblemDescription.replace("%s%", objMethod.getName());
                                holder.registerProblem(methodNameNode, strWarning, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }

                /** inspect fields */
                for (Field objField : clazz.getOwnFields()) {
                    /** TODO: re-evaluate if JB added access api to fields */
                    if (!objField.isConstant()) {
                        /** find modifiers list */
                        String strModifiers = null;
                        for (PsiElement objChild : objField.getParent().getChildren()) {
                            if (objChild instanceof PhpModifierList) {
                                strModifiers = objChild.getText();
                                break;
                            }
                        }

                        if (null != strModifiers && null != objField.getNameIdentifier()) {
                            /** scan modifiers defined */
                            /** TODO: re-evaluate if JB completed modifiers list construction */
                            boolean hasAccessModifiers =
                                    strModifiers.contains("public") ||
                                    strModifiers.contains("protected") ||
                                    strModifiers.contains("private");

                            /** report issues */
                            if (!hasAccessModifiers) {
                                String strWarning = strProblemDescription.replace("%s%", objField.getName());
                                holder.registerProblem(objField.getNameIdentifier(), strWarning, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
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
