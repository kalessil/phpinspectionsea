package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SenselessCommaInArrayDefinitionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "PHP will ignore this comma, so it can be dropped";

    @NotNull
    public String getDisplayName() {
        return "Code style: unnecessary comma in array definition";
    }

    @NotNull
    public String getShortName() {
        return "SenselessCommaInArrayDefinitionInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {
                PsiElement objExpressionToTest = expression.getLastChild().getPrevSibling();
                if (null == objExpressionToTest) {
                    return;
                }

                if (objExpressionToTest instanceof PsiWhiteSpace) {
                    objExpressionToTest = objExpressionToTest.getPrevSibling();
                }
                if (null == objExpressionToTest) {
                    return;
                }


                if(!objExpressionToTest.getText().equals(",")) {
                    return;
                }

                holder.registerProblem(expression.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
