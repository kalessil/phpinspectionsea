package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TraitsPropertiesConflictsInspector extends PhpInspection {
    private static final String messagePattern = "'%c%' and '%t%' define the same property ($%p%).";

    @NotNull
    @Override
    public String getShortName() {
        return "TraitsPropertiesConflictsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (this.shouldSkipAnalysis(clazz, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                /* ensure there are traits being used at all */
                final PhpClass[] traits = clazz.getTraits();
                if (traits.length == 0) {
                    return;
                }

                /* check conflict with own fields */
                for (final Field ownField : clazz.getOwnFields()) {
                    /* get own field name and default value */
                    final String ownFieldName = ownField.getName();
                    if (ownFieldName.isEmpty() || ownField.isConstant() || ownField.getModifier().isAbstract()) {
                        continue;
                    }
                    /* ensure field doesn't have any user-land annotations */
                    final PhpDocTag[] tags  = PsiTreeUtil.getChildrenOfType(ownField.getDocComment(), PhpDocTag.class);
                    final boolean annotated = tags != null && Arrays.stream(tags).anyMatch(t -> !t.getName().equals(t.getName().toLowerCase()));
                    if (annotated) {
                        continue;
                    }

                    final PsiElement ownFieldDefault = OpenapiResolveUtil.resolveDefaultValue(ownField);
                    for (final PhpClass trait : traits) {
                        final Field traitField = OpenapiResolveUtil.resolveField(trait, ownFieldName);
                        if (traitField != null && ExpressionSemanticUtil.getBlockScope(traitField) == trait) {
                            final PsiElement traitFieldDefault = OpenapiResolveUtil.resolveDefaultValue(traitField);

                            final boolean isError;
                            if (ownFieldDefault == null || traitFieldDefault == null) {
                                isError = traitFieldDefault != ownFieldDefault;
                            } else {
                                isError = !OpenapiEquivalenceUtil.areEqual(traitFieldDefault, ownFieldDefault);
                            }

                            /* error case already covered by the IDEs */
                            final PsiElement ownFieldNameNode = NamedElementUtil.getNameIdentifier(ownField);
                            if (!isError && ownFieldNameNode != null) {
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
                for (final PsiElement child: clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (ClassReference reference :PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof PhpClass) {
                                useReportTargets.putIfAbsent((PhpClass) resolved, reference);
                            }
                        }
                    }
                }
                final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                if (parent == null || useReportTargets.isEmpty()) {
                    useReportTargets.clear();
                    return;
                }

                /* iterate parent non-private fields to find conflicting properties */
                for (final Field parentField : parent.getFields()) {
                    final String parentFieldName = parentField.getName();
                    final PhpModifier modifier   = parentField.getModifier();
                    if (parentFieldName.isEmpty() || parentField.isConstant() || modifier.isPrivate() || modifier.isAbstract()) {
                        continue;
                    }

                    final PsiElement parentFieldDefault = OpenapiResolveUtil.resolveDefaultValue(parentField);
                    for (final PhpClass trait : traits) {
                        final Field traitField = OpenapiResolveUtil.resolveField(trait, parentFieldName);
                        if (traitField != null && ExpressionSemanticUtil.getBlockScope(traitField) == trait) {
                            final PsiElement traitFieldDefault = OpenapiResolveUtil.resolveDefaultValue(traitField);

                            final boolean isError;
                            if (parentFieldDefault == null || traitFieldDefault == null) {
                                isError = traitFieldDefault != parentFieldDefault;
                            } else {
                                isError = !OpenapiEquivalenceUtil.areEqual(traitFieldDefault, parentFieldDefault);
                            }

                            final PsiElement reportTarget = useReportTargets.get(trait);
                            if (reportTarget != null) {
                                final String message = messagePattern
                                        .replace("%p%", parentFieldName)
                                        .replace("%t%", trait.getName())
                                        .replace("%c%", clazz.getName());
                                holder.registerProblem(
                                    reportTarget,
                                    message,
                                    isError ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.WEAK_WARNING
                                );
                            }
                            break;
                        }
                    }
                }
                useReportTargets.clear();
            }
        };
    }
}
