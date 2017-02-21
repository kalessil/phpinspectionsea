package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;


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

                FieldReference fieldReference = (FieldReference) variable;
                if (!fieldReference.getReferenceType().isStatic()) {
                    return;
                }

                PhpExpression classReference = fieldReference.getClassReference();
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
                PhpClass assignStatementContainingClass = ((Method) scope).getContainingClass();

                if (assignStatementContainingClass == null || callyClassFqn == null || !callyClassFqn.equals(assignStatementContainingClass.getFQN())) {
                    // Property is modified in the other class method
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                    return;
                }


                // Special case for inheritance. Check if property is declared in the cally class
                PsiElement field = fieldReference.resolve();
                if (!(field instanceof Field)) {
                    // Can not detect real property declaration
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                PhpClass propertyDeclarationClass = ((Field) field).getContainingClass();
                if (propertyDeclarationClass==null || !propertyDeclarationClass.getFQN().equals(callyClassFqn)) {
                    holder.registerProblem(assignmentExpression, message, ProblemHighlightType.WEAK_WARNING);
                }


            }
        };

    }

}
