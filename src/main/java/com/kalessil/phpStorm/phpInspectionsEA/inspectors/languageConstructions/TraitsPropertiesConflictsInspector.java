package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

public class TraitsPropertiesConflictsInspector extends BasePhpInspection {
    private static final String messageWarning = "'%c%' and '%t%' define the same property ($%p%).";
    private static final String messageFatal   = "'%c%' and '%t%' define the same property ($%p%).";

    @NotNull
    public String getShortName() {
        return "TraitsPropertiesConflictsInspection";
    }

        @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /* ensure there are traits being used at all */
                final PhpClass[] traits = clazz.getTraits();
                if (0 == traits.length) {
                    return;
                }

                for (Field ownField : clazz.getOwnFields()) {
                    /* get own field name and default value */
                    final PsiElement ownFieldNameNode = NamedElementUtil.getNameIdentifier(ownField);
                    if (null == ownFieldNameNode) {
                        continue;
                    }
                    final String ownFieldName         = ownField.getName();
                    final PsiElement ownFieldDefault  = ownField.getDefaultValue();

                    for (PhpClass trait : traits) {
                        final Field traitField = trait.findFieldByName(ownFieldName, false);
                        if (null != traitField) {
                            final PsiElement traitFieldDefault = traitField.getDefaultValue();

                            final boolean isError;
                            if (null == ownFieldDefault || null == traitFieldDefault) {
                                isError = traitFieldDefault != ownFieldDefault;
                            } else {
                                isError = !PsiEquivalenceUtil.areElementsEquivalent(traitFieldDefault, ownFieldDefault);
                            }

                            /* error case already covered by the IDEs */
                            if (!isError) {
                                final String message = messageWarning
                                    .replace("%p%", ownFieldName)
                                    .replace("%t%", trait.getName())
                                    .replace("%c%", clazz.getName())
                                ;
                                holder.registerProblem(ownFieldNameNode, message, ProblemHighlightType.WEAK_WARNING);
                            }
                            break;
                        }
                    }
                }

                /* now find use trait statements, and check clazz parent to report use statements */
            }
        };
    }
}
