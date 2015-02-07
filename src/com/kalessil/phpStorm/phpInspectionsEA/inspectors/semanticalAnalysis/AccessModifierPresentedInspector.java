package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class AccessModifierPresentedInspector extends BasePhpInspection {
    private static final String strProblemDescription = "%s% shall be declared with access modifier.";

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /** inspect methods */
                for (Method objMethod : clazz.getOwnMethods()) {
                    /** find modifiers list */
                    String strModifiers = null;
                    for (PsiElement objChild : objMethod.getChildren()) {
                        if (objChild instanceof PhpModifierList) {
                            strModifiers = objChild.getText();
                            break;
                        }
                    }

                    if (null != strModifiers && null != objMethod.getNameIdentifier()) {
                        /** scan modifiers defined */
                        /** scan modifiers defined */
                        /** TODO: re-evaluate if JB completed modifiers list construction */
                        boolean hasAccessModifiers =
                                strModifiers.contains("public") ||
                                strModifiers.contains("protected") ||
                                strModifiers.contains("private");

                        /** scan modifiers defined */
                        if (!hasAccessModifiers) {
                            String strWarning = strProblemDescription.replace("%s%", objMethod.getName());
                            holder.registerProblem(objMethod.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }

                /** inspect fields */
                for (Field objField : clazz.getOwnFields()) {
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
                            holder.registerProblem(objField.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }
}
