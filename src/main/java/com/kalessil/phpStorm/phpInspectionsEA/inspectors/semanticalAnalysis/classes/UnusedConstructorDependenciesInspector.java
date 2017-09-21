package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
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
        return new PhpElementVisitor() {
            /**
             * Extracts private fields list as map name => field object, so we could use name as glue
             * and direct comparision of object pointers unique for field objects.
             */
            private Map<String, Field> getPrivateFields(@NotNull PhpClass clazz) {
                final Map<String, Field> privateFields = new HashMap<>();

                for (Field field : clazz.getOwnFields()) {
                    final PhpModifier modifiers = field.getModifier();
                    if (field.isConstant() || !modifiers.isPrivate() || modifiers.isStatic()) {
                        continue;
                    }

                    privateFields.put(field.getName(), field);
                }

                return  privateFields;
            }

            /**
             * Extracts private fields references from method as field name => reference objects, so we could use name as glue.
             */
            private Map<String, List<FieldReference>> getFieldReferences(@NotNull Method method, @NotNull Map<String, Field> privateFields) {
                final Map<String, List<FieldReference>> filteredReferences = new HashMap<>();
                /* not all methods needs to be analyzed */
                if (method.isAbstract()) {
                    return filteredReferences;
                }

                final Collection<FieldReference> references = PsiTreeUtil.findChildrenOfType(method, FieldReference.class);
                for (FieldReference ref : references) {
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

            /**
             * Orchestrates extraction of references from methods
             */
            private Map<String, List<FieldReference>> getMethodsFieldReferences(@NotNull Method constructor, @NotNull Map<String, Field> privateFields) {
                final Map<String, List<FieldReference>> filteredReferences = new HashMap<>();

                /* collect methods to inspect, incl. once from traits */
                final List<Method> methodsToCheck = new ArrayList<>();
                //noinspection ConstantConditions as this checked in visitPhpMethod
                Collections.addAll(methodsToCheck, constructor.getContainingClass().getOwnMethods());
                for (final PhpClass trait : constructor.getContainingClass().getTraits()) {
                    Collections.addAll(methodsToCheck, trait.getOwnMethods());
                }

                /* find references */
                for (Method method : methodsToCheck) {
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

            public void visitPhpMethod(Method method) {
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
                    final Map<String, List<FieldReference>> otherReferences = getMethodsFieldReferences(constructor, clazzPrivateFields);
                    /* methods's references being identified, time to re-visit constructor's references */
                    constructorsReferences.forEach((fieldName, fields) -> {
                        /* field is not used, report in constructor, PS will cover unused fields detection */
                        if (!otherReferences.containsKey(fieldName)) {
                            fields.forEach(reference -> holder.registerProblem(reference, message));
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
        };
    }
}
