package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PropertyCanBeStaticInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This property initialization seems to be quite 'heavy', probably it should be defined as static";

    @NotNull
    public String getShortName() {
        return "PropertyCanBeStaticInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                for (Field field : clazz.getOwnFields()) {
                    /** due to lack of api get raw text with all modifiers */
                    String strModifiers = null;
                    for (PsiElement objChild : field.getParent().getChildren()) {
                        if (objChild instanceof PhpModifierList) {
                            strModifiers = objChild.getText();
                            break;
                        }
                    }
                    /** skip static and public variables - they shall not be changed via constructor */
                    if (
                        StringUtil.isEmpty(strModifiers) ||
                        strModifiers.contains("static") || strModifiers.contains("public") ||
                        !(field.getDefaultValue() instanceof ArrayCreationExpression)
                    ) {
                        continue;
                    }


                    /* look into array for key-value pairs */
                    /** TODO: merge into next loop */
                    int intArrayOrStringCount = 0;
                    for (ArrayHashElement objEntry : ((ArrayCreationExpression) field.getDefaultValue()).getHashElements()) {
                        PhpPsiElement item = objEntry.getValue();
                        if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                            ++intArrayOrStringCount;
                        }

                        if (intArrayOrStringCount == 3) {
                            holder.registerProblem(field.getParent(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                            break;
                        }
                    }
                    /* look into array for value only pairs */
                    if (intArrayOrStringCount < 3) {
                        for (PsiElement objEntry : field.getDefaultValue().getChildren()) {
                            if (objEntry instanceof PhpPsiElement && !(objEntry instanceof ArrayHashElement)) {
                                PhpPsiElement item = ((PhpPsiElement) objEntry).getFirstPsiChild();
                                if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                                    ++intArrayOrStringCount;
                                }

                                if (intArrayOrStringCount == 3) {
                                    holder.registerProblem(field.getParent(), strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
