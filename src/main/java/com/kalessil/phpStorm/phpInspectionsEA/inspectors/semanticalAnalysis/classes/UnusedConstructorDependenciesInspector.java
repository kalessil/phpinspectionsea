package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnusedConstructorDependenciesInspector extends BasePhpInspection {
    private static final String message = "Property is used only in constructor, perhaps we are dealing with dead code here.";

    @NotNull
    public String getShortName() {
        return "UnusedConstructorDependenciesInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @NotNull
            private Map<String, Field> getPrivateFields(@NotNull PhpClass clazz) {
                final Map<String, Field> privateFields = new HashMap<>();
                for (final Field field : clazz.getOwnFields()) {
                    if (!field.isConstant()) {
                        final PhpModifier modifiers = field.getModifier();
                        if (modifiers.isPrivate() && !modifiers.isStatic()) {
                            final PhpDocTag[] tags  = PsiTreeUtil.getChildrenOfType(field.getDocComment(), PhpDocTag.class);
                            final boolean annotated = tags != null && Arrays.stream(tags).anyMatch(t -> !t.getName().equals(t.getName().toLowerCase()));
                            if (!annotated) {
                                privateFields.put(field.getName(), field);
                            }
                        }
                    }
                }
                return privateFields;
            }

            @NotNull
            private Map<String, List<FieldReference>> getFieldReferences(@NotNull Method method, @NotNull Map<String, Field> privateFields) {
                final Map<String, List<FieldReference>> filteredReferences = new HashMap<>();
                /* not all methods needs to be analyzed */
                if (method.isAbstract()) {
                    return filteredReferences;
                }

                final Collection<FieldReference> references = PsiTreeUtil.findChildrenOfType(method, FieldReference.class);
                for (final FieldReference ref : references) {
                    /* if field name not in given list, skip heavy resolving */
                    final String fieldName = ref.getName();
                    if (null == fieldName || !privateFields.containsKey(fieldName)) {
                        continue;
                    }

                    /* if not resolved or not in given list, continue */
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(ref);
                    if (!(resolved instanceof Field) || !privateFields.containsValue(resolved)) {
                        continue;
                    }

                    /* bingo, store newly found reference */
                    filteredReferences.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(ref);
                }
                references.clear();

                return filteredReferences;
            }

            @NotNull
            private Map<String, List<FieldReference>> getMethodsFieldReferences(
                    @NotNull PhpClass clazz,
                    @NotNull Method constructor,
                    @NotNull Map<String, Field> privateFields
            ) {
                final Map<String, List<FieldReference>> filteredReferences = new HashMap<>();

                /* collect methods to inspect, incl. once from traits */
                final List<Method> methodsToCheck = new ArrayList<>();
                Collections.addAll(methodsToCheck, clazz.getOwnMethods());
                for (final PhpClass trait : clazz.getTraits()) {
                    Collections.addAll(methodsToCheck, trait.getOwnMethods());
                }

                /* find references */
                for (final Method method : methodsToCheck) {
                    if (method == constructor) {
                        continue;
                    }

                    final Map<String, List<FieldReference>> innerReferences = getFieldReferences(method, privateFields);
                    if (!innerReferences.isEmpty()) {
                        /* merge method's scan results into common container */
                        innerReferences.forEach((fieldName, fields) -> {
                            filteredReferences
                                .computeIfAbsent(fieldName, name -> new ArrayList<>())
                                .addAll(fields);
                            fields.clear();
                        });
                        innerReferences.clear();
                    }
                }
                methodsToCheck.clear();

                return filteredReferences;
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                /* filter classes which needs to be analyzed */
                final PhpClass clazz = method.getContainingClass();
                if (
                    null == clazz || clazz.isInterface() || clazz.isTrait() ||
                    null == clazz.getOwnConstructor() ||
                    0 == clazz.getOwnFields().length || 0 == clazz.getOwnMethods().length
                ) {
                    return;
                }

                /* run inspection only in constructors and if own private fields being defined */
                final Method constructor = clazz.getOwnConstructor();
                if (method != constructor) {
                    return;
                }
                final Map<String, Field> clazzPrivateFields = this.getPrivateFields(clazz);
                if (clazzPrivateFields.isEmpty()) {
                    return;
                }

                /* === intensive part : extract references === */
                final Map<String, List<FieldReference>> constructorsReferences = getFieldReferences(constructor, clazzPrivateFields);
                if (!constructorsReferences.isEmpty()) {
                    /* constructor's references being identified */
                    final Map<String, List<FieldReference>> otherReferences = getMethodsFieldReferences(clazz, constructor, clazzPrivateFields);
                    /* methods's references being identified, time to re-visit constructor's references */
                    constructorsReferences.forEach((fieldName, fields) -> {
                        /* field is not used, report in constructor, IDE detects unused fields */
                        if (!otherReferences.containsKey(fieldName)) {
                            this.doSmartReport(holder, fields);
                        }
                        fields.clear();
                    });
                    /* release references found in the methods */
                    otherReferences.values().forEach(List::clear);
                    otherReferences.clear();
                    /* release references found in the constructor */
                    constructorsReferences.clear();
                }
            }

            private void doSmartReport(@NotNull ProblemsHolder holder, @NotNull List<FieldReference> fields) {
                int assignmentsCount  = 0;
                int methodCallsCount  = 0;
                FieldReference target = null;
                for (final FieldReference reference : fields) {
                    final PsiElement parent = reference.getParent();
                    if (parent instanceof MethodReference) {
                        /* calls performed on the field */
                        final MethodReference parentCall = (MethodReference) parent;
                        if (parentCall.getFirstPsiChild() == reference) {
                            ++methodCallsCount;
                        }
                    } else if (OpenapiTypesUtil.isAssignment(parent)) {
                        /* field value overwrites */
                        final AssignmentExpression parentAssignment = (AssignmentExpression) parent;
                        if (parentAssignment.getVariable() == reference) {
                            target = reference;
                            if (++assignmentsCount > 1) {
                                break;
                            }
                        }
                    } else {
                        /* another expression, break the loop */
                        break;
                    }
                }
                if (target != null && assignmentsCount == 1 && fields.size() == assignmentsCount + methodCallsCount) {
                    /* report only assignment, when single write + multiple invocations are performed */
                    holder.registerProblem(target, message);
                } else {
                    /* report all entries, general behaviour */
                    fields.forEach(reference -> holder.registerProblem(reference, message));
                }
            }
        };
    }
}
