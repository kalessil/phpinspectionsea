package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class UnusedConstructorDependenciesInspector extends BasePhpInspection {
    private static final String message = "Property is used only in constructor, perhaps we are dealing with dead code here";

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
            private HashMap<String, Field> getPrivateFields(@NotNull PhpClass clazz) {
                final HashMap<String, Field> privateFields = new HashMap<String, Field>();

                final Field[] ownFields = clazz.getOwnFields();
                if (ownFields.length > 0) {
                    for (Field field : ownFields) {
                        if (field.isConstant() || !field.getModifier().isPrivate() || field.getModifier().isStatic()) {
                            continue;
                        }

                        privateFields.put(field.getName(), field);
                    }
                }

                return  privateFields;
            }

            /**
             * Extracts private fields references from method as field name => reference objects, so we could use name as glue.
             */
            private HashMap<String, LinkedList<FieldReference>> getFieldReferences(@NotNull Method method, @NotNull HashMap<String, Field> privateFields) {
                final HashMap<String, LinkedList<FieldReference>> filteredReferences = new HashMap<String, LinkedList<FieldReference>>();
                /* not all methods needs to be analyzed */
                if (method.isAbstract() || method.isStatic()) {
                    return filteredReferences;
                }

                final Collection<FieldReference> references = PsiTreeUtil.findChildrenOfType(method, FieldReference.class);
                if (references.size() > 0) {
                    for (FieldReference ref : references) {
                        /* if field name not in given list, skip heavy resolving */
                        final String fieldName = ref.getName();
                        if (StringUtil.isEmpty(fieldName) || !privateFields.containsKey(fieldName)) {
                            continue;
                        }

                        /* if not resolved or not in given list, continue */
                        final PsiElement resolved = ref.resolve();
                        if (!(resolved instanceof Field) || !privateFields.containsValue(resolved)) {
                            continue;
                        }

                        /* bingo, store newly found reference */
                        if (!filteredReferences.containsKey(fieldName)) {
                            filteredReferences.put(fieldName, new LinkedList<FieldReference>());
                        }
                        filteredReferences.get(fieldName).add(ref);
                    }

                    references.clear();
                }

                return filteredReferences;
            }

            /**
             * Orchestrates extraction of references from methods
             */
            private HashMap<String, LinkedList<FieldReference>> getMethodsFieldReferences(@NotNull Method constructor, @NotNull HashMap<String, Field> privateFields) {
                final HashMap<String, LinkedList<FieldReference>> filteredReferences = new HashMap<String, LinkedList<FieldReference>>();

                //noinspection ConstantConditions as this checked in visitPhpMethod
                for (Method method : constructor.getContainingClass().getOwnMethods()) {
                    if (method == constructor || method.isAbstract() || method.isStatic()) {
                        continue;
                    }

                    final HashMap<String, LinkedList<FieldReference>> methodsReferences = getFieldReferences(method, privateFields);
                    if (methodsReferences.size() > 0) {
                        /* merge method's scan results into common container */
                        for (String fieldName : methodsReferences.keySet()) {
                            if (!filteredReferences.containsKey(fieldName)) {
                                filteredReferences.put(fieldName, new LinkedList<FieldReference>());
                            }
                            filteredReferences
                                    .get(fieldName)
                                    .addAll(methodsReferences.get(fieldName));
                        }

                        /* release references found in the method as they in common container now */
                        for (LinkedList<FieldReference> references : methodsReferences.values()) {
                            references.clear();
                        }
                        methodsReferences.clear();
                    }
                }

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
                final HashMap<String, Field> clazzPrivateFields =  this.getPrivateFields(clazz);
                if (0 == clazzPrivateFields.size()) {
                    return;
                }

                /* === intensive part : extract references === */
                final HashMap<String, LinkedList<FieldReference>> constructorsReferences = getFieldReferences(constructor, clazzPrivateFields);
                if (constructorsReferences.size() > 0) {
                    /* constructor's references being identified */
                    final HashMap<String, LinkedList<FieldReference>> otherReferences = getMethodsFieldReferences(constructor, clazzPrivateFields);
                    if (otherReferences.size() > 0) {
                        /* methods's references being identified, time to re-visit constructor's references */
                        for (String fieldName : constructorsReferences.keySet()) {
                            /* field is used, we do nothing more */
                            if (otherReferences.containsKey(fieldName)) {
                                continue;
                            }

                            /* report directly expressions in constructor, PS will cover unused fields detection */
                            for (FieldReference reference : constructorsReferences.get(fieldName)) {
                                holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING);
                            }
                        }

                        /* release references found in the methods */
                        for (LinkedList<FieldReference> references : otherReferences.values()) {
                            references.clear();
                        }
                        otherReferences.clear();
                    }

                    /* release references found in the constructor */
                    for (LinkedList<FieldReference> references : constructorsReferences.values()) {
                        references.clear();
                    }
                    constructorsReferences.clear();
                }
            }
        };
    }
}
