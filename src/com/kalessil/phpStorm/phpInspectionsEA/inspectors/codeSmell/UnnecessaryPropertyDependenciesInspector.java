package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Ref;
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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by funivan
 */
public class UnnecessaryPropertyDependenciesInspector extends BasePhpInspection {
    private static final String message = "Property used only inside constructor #ref";

    @NotNull
    public String getShortName() {
        return "UnnecessaryPropertyDependenciesInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpMethod(Method method) {
                if (!isMethodConstructor(method)) {
                    return;
                }


                PhpClass targetClass = method.getContainingClass();

                Collection<FieldReference> fieldReferences = PsiTreeUtil.findChildrenOfType(method, FieldReference.class);
                ArrayList<String> visited = new ArrayList<String>();
                for (FieldReference fieldReference : fieldReferences) {

                    PsiElement resolvedElement = fieldReference.resolve();
                    if (!(resolvedElement instanceof Field)) {
                        continue;
                    }

                    Field field = (Field) resolvedElement;

                    if (visited.contains(field.getName())) {
                        // check field only once
                        continue;
                    }

                    visited.add(field.getName());

                    if (!field.getContainingClass().equals(targetClass)) {
                        // Skip fields from other classes
                        continue;
                    }

                    if (!field.getModifier().isPrivate()) {
                        continue;
                    }


                    PsiElement namePsiElement = PhpPsiUtil.getNamePsiElement(field);
                    if (namePsiElement == null) {
                        continue;
                    }

                    boolean res = UnnecessaryPropertyDependenciesInspector.isFieldUsedOutsideTheConstructor(field);
                    if (!res) {
                        holder.registerProblem(field, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, LocalQuickFix.EMPTY_ARRAY);
                    }

                }

            }


        };
    }

    public static boolean isMethodConstructor(@Nullable Method method) {
        if (method == null) {
            return false;
        }
        return method.getName().equals("__construct");
    }

    private static boolean isFieldUsedOutsideTheConstructor(@NotNull final Field field) {
        PhpClass containingClass = field.getContainingClass();
        if (containingClass == null || containingClass.isTrait()) {
            return true;
        }

        final CharSequence fieldName = field.getNameCS();

        final Ref foundReference = new Ref(Boolean.valueOf(false));

        containingClass.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {

                if (((Boolean) foundReference.get()).booleanValue() == true) {
                    return;
                }

                if (element instanceof FieldReference) {
                    visitPhpFieldReference((FieldReference) element);
                }
                super.visitElement(element);
            }

            public void visitPhpFieldReference(FieldReference fieldReference) {
                Method parentMethod = PsiTreeUtil.getParentOfType(fieldReference, Method.class);
                if (isMethodConstructor(parentMethod)) {
                    return;
                }
                if (PhpLangUtil.equalsFieldNames(fieldName, fieldReference.getCanonicalText())) {
                    foundReference.set(Boolean.valueOf(true));
                }
            }
        });

        return ((Boolean) foundReference.get()).booleanValue();
    }

}
