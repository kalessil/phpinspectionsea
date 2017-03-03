package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TraitsPropertiesConflictsInspector extends BasePhpInspection {
    private static final String messagePattern = "'%c%' and '%t%' define the same property ($%p%).";

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

                /* check conflict with own fields */
                for (Field ownField : clazz.getOwnFields()) {
                    /* get own field name and default value */
                    final PsiElement ownFieldNameNode = NamedElementUtil.getNameIdentifier(ownField);
                    if (null == ownFieldNameNode || ownField.isConstant() || ownField.getModifier().isAbstract()) {
                        continue;
                    }
                    final String ownFieldName        = ownField.getName();
                    final PsiElement ownFieldDefault = ownField.getDefaultValue();

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
                                final String message = messagePattern
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


                /* check parent class accessibility and map use statements with traits for reporting */
                final Map<PhpClass, PsiElement> useReportTargets = new HashMap<>();
                for (PsiElement child: clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (ClassReference reference :PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
                            final PsiElement resolved = reference.resolve();
                            if (resolved instanceof PhpClass) {
                                useReportTargets.putIfAbsent((PhpClass) resolved, reference);
                            }
                        }
                    }
                }
                final PhpClass parent = clazz.getSuperClass();
                if (null == parent || useReportTargets.isEmpty()) {
                    useReportTargets.clear();
                    return;
                }

                /* iterate parent non-private fields to find conflicting properties */
                for (Field parentField : parent.getFields()) {
                    final PsiElement parentFieldNameNode = NamedElementUtil.getNameIdentifier(parentField);
                    final PhpModifier modifier           = parentField.getModifier();
                    if (null == parentFieldNameNode || parentField.isConstant() || modifier.isPrivate() || modifier.isAbstract()) {
                        continue;
                    }
                    final String parentFieldName        = parentField.getName();
                    final PsiElement parentFieldDefault = parentField.getDefaultValue();

                    for (PhpClass trait : traits) {
                        final Field traitField = trait.findFieldByName(parentFieldName, false);
                        if (null != traitField) {
                            final PsiElement traitFieldDefault = traitField.getDefaultValue();

                            final boolean isError;
                            if (null == parentFieldDefault || null == traitFieldDefault) {
                                isError = traitFieldDefault != parentFieldDefault;
                            } else {
                                isError = !PsiEquivalenceUtil.areElementsEquivalent(traitFieldDefault, parentFieldDefault);
                            }

                            final String message = messagePattern
                                .replace("%p%", parentFieldName)
                                .replace("%t%", trait.getName())
                                .replace("%c%", clazz.getName());
                            holder.registerProblem(useReportTargets.get(trait), message,
                                isError ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.WEAK_WARNING);
                            break;
                        }
                    }
                }
                useReportTargets.clear();
            }
        };
    }
}
