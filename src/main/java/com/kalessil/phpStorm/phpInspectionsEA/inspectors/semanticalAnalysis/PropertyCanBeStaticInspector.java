package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PropertyCanBeStaticInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This property initialization seems to be quite 'heavy', probably it should be defined as static.";

    @NotNull
    public String getShortName() {
        return "PropertyCanBeStaticInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /* parent class might already introduce fields */
                final PhpClass parent = clazz.getSuperClass();

                for (Field field : clazz.getOwnFields()) {
                    /* if we can report field at all */
                    final PsiElement fieldName = field.getNameIdentifier();
                    if (null == fieldName) {
                        continue;
                    }
                    /* do not process overriding, there is an inspection for this */
                    if (null != parent && null != parent.findFieldByName(field.getName(), false)) {
                        continue;
                    }


                    /* skip static and public variables - they should not be changed via constructor */
                    final PhpModifier modifier = field.getModifier();
                    if (
                        modifier.isStatic() || modifier.isPublic() ||
                        !(field.getDefaultValue() instanceof ArrayCreationExpression)
                    ) {
                        continue;
                    }


                    /* look into array for key-value pairs */
                    /* TODO: merge into next loop */
                    int intArrayOrStringCount = 0;
                    for (ArrayHashElement objEntry : ((ArrayCreationExpression) field.getDefaultValue()).getHashElements()) {
                        PhpPsiElement item = objEntry.getValue();
                        if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                            ++intArrayOrStringCount;
                        }

                        if (intArrayOrStringCount == 3) {
                            holder.registerProblem(fieldName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
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
                                    holder.registerProblem(fieldName, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
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
