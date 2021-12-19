package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
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

public class TraitsPropertiesConflictsInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' and '%s' define the same property ($%s).";

    @NotNull
    @Override
    public String getShortName() {
        return "TraitsPropertiesConflictsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Traits properties conflicts resolution";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                /* ensure there are traits being used at all */
                final PhpClass[] traits = clazz.getTraits();
                if (traits.length == 0) {
                    return;
                }

                /* check conflict with own fields */
                for (final Field ownField : clazz.getOwnFields()) {
                    final String ownFieldName = ownField.getName();
                    if (! ownFieldName.isEmpty() && ! ownField.isConstant() && ! this.isDocBlockProperty(ownField, clazz)) {
                        final PhpModifier modifier = ownField.getModifier();
                        if (! modifier.isAbstract() && ! this.isAnnotated(ownField)) {
                            final PsiElement ownFieldDefault = OpenapiResolveUtil.resolveDefaultValue(ownField);
                            for (final PhpClass trait : traits) {
                                final Field traitField = OpenapiResolveUtil.resolveField(trait, ownFieldName);
                                if (traitField != null && ! this.isDocBlockProperty(traitField, trait)) {
                                    final PsiElement traitFieldDefault = OpenapiResolveUtil.resolveDefaultValue(traitField);

                                    final boolean isError;
                                    if (ownFieldDefault == null || traitFieldDefault == null) {
                                        isError = traitFieldDefault != ownFieldDefault;
                                    } else {
                                        isError = ! OpenapiEquivalenceUtil.areEqual(traitFieldDefault, ownFieldDefault);
                                    }

                                    /* error case already covered by the IDEs */
                                    final PsiElement ownFieldNameNode = NamedElementUtil.getNameIdentifier(ownField);
                                    if (!isError && ownFieldNameNode != null) {
                                        holder.registerProblem(
                                                ownFieldNameNode,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), clazz.getName(), trait.getName(), ownFieldName),
                                                ProblemHighlightType.WEAK_WARNING
                                        );
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }

                /* check parent class accessibility and map use statements with traits for reporting */
                final Map<PhpClass, PsiElement> useReportTargets = new HashMap<>();
                for (final PsiElement child: clazz.getChildren()) {
                    if (child instanceof PhpUseList) {
                        for (final ClassReference reference: PsiTreeUtil.findChildrenOfType(child, ClassReference.class)) {
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
                    if (! parentFieldName.isEmpty() && ! parentField.isConstant() && ! this.isDocBlockProperty(parentField, parent)) {
                        final PhpModifier modifier = parentField.getModifier();
                        if (! modifier.isPrivate() && ! modifier.isAbstract()) {
                            final PsiElement parentFieldDefault = OpenapiResolveUtil.resolveDefaultValue(parentField);
                            for (final PhpClass trait : traits) {
                                final Field traitField = OpenapiResolveUtil.resolveField(trait, parentFieldName);
                                if (traitField != null && ! this.isDocBlockProperty(traitField, trait)) {
                                    final PsiElement traitFieldDefault = OpenapiResolveUtil.resolveDefaultValue(traitField);

                                    final boolean isError;
                                    if (parentFieldDefault == null || traitFieldDefault == null) {
                                        isError = traitFieldDefault != parentFieldDefault;
                                    } else {
                                        isError = ! OpenapiEquivalenceUtil.areEqual(traitFieldDefault, parentFieldDefault);
                                    }

                                    final PsiElement reportTarget = useReportTargets.get(trait);
                                    if (reportTarget != null) {
                                        holder.registerProblem(
                                                reportTarget,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), clazz.getName(), trait.getName(), parentFieldName),
                                                isError ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.WEAK_WARNING
                                        );
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                useReportTargets.clear();
            }

            private boolean isAnnotated(@NotNull Field ownField) {
                final PhpDocTag[] tags = PsiTreeUtil.getChildrenOfType(ownField.getDocComment(), PhpDocTag.class);
                return tags != null && Arrays.stream(tags).anyMatch(t -> ! t.getName().equals(t.getName().toLowerCase()));
            }

            private boolean isDocBlockProperty(@NotNull Field field, @NotNull PhpClass clazz) {
                return ExpressionSemanticUtil.getBlockScope(field) != clazz;
            }
        };
    }
}
