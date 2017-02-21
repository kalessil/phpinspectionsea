package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class DisallowWritingIntoStaticPropertiesInspector extends BasePhpInspection {

    private static final String message = "Static property should be modified only inside the source class";

    @NotNull
    public String getShortName() {
        return "DisallowWritingIntoStaticPropertiesInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean b) {


        return new BasePhpElementVisitor() {

            @Override
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PhpPsiElement variable = assignmentExpression.getVariable();
                if (!(variable instanceof FieldReference)) {
                    return;
                }

                if (!((FieldReference) variable).getReferenceType().isStatic()) {
                    return;
                }

                PhpExpression classReference = ((FieldReference) variable).getClassReference();
                if (!(classReference instanceof ClassReference)) {
                    return;
                }

                String name = classReference.getName();

                if (name == null || name.equals("self")) {
                    // Property is changed inside the class
                    return;
                }

                final Function scope = ExpressionSemanticUtil.getScope(assignmentExpression);

                if (name.equals("static") || !(scope instanceof Method)) {
                    // Property is changed with static keyword or outside the class method
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                    return;
                }


                String callyClassFqn = ((ClassReference) classReference).getFQN();
                PhpClass containingClass = ((Method) scope).getContainingClass();


                if (containingClass == null || callyClassFqn == null || !callyClassFqn.equals(containingClass.getFQN())) {
                    // Property is modified in the other class method
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                    return;
                }


                // Special case for inheritance. Check if property is declared in the cally class
                String propertyName = variable.getName();
                if (!isPropertyDefinedInClass(callyClassFqn, propertyName, holder.getProject())) {
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                }


            }
        };

    }

    private boolean isPropertyDefinedInClass(String callyClassFqn, String variableName, Project project) {
        final Collection<PhpClass> classes = PhpIndexUtil.getObjectInterfaces(callyClassFqn, PhpIndex.getInstance(project), true);

        if (classes.size() == 0) {
            return false;
        }

        for (PhpClass phpClass : classes) {
            MultiMap<CharSequence, Field> ownFields = phpClass.getOwnFieldMap();
            if (ownFields.containsKey(variableName)) {
                return true;
            }

        }

        return false;
    }
}
