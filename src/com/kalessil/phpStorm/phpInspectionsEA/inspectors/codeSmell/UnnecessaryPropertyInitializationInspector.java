package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UnnecessaryPropertyInitializationInspector extends BasePhpInspection {
    private static final String message = "This construct is probably unnecessary as property seems to be not used";

    @NotNull
    public String getShortName() {
        return "UnnecessaryPropertyInitializationInspection";
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
                        if (field.isConstant() || !field.getModifier().getAccess().isPrivate()) {
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

                        /* bingo, store reference */
                        if (!filteredReferences.containsKey(fieldName)) {
                            filteredReferences.put(fieldName, new LinkedList<FieldReference>());
                        }
                        filteredReferences.get(fieldName).add(ref);
                    }

                    references.clear();
                }


                return filteredReferences;
            }

            public void visitPhpMethod(Method method) {
                /* filter classes which needs to be analyzed */
                final PhpClass clazz = method.getContainingClass();
                if (
                    null == clazz || clazz.isInterface() || clazz.isTrait() ||
                    null == clazz.getOwnConstructor() || 0 == clazz.getOwnFields().length
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

                    /* === report here === */

                    /* release references found in the constructor */
                    for (LinkedList<FieldReference> refs : constructorsReferences.values()) {
                        refs.clear();
                    }
                    constructorsReferences.clear();
                }





                //holder.registerProblem(field, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, LocalQuickFix.EMPTY_ARRAY);
            }
        };
    }
}
