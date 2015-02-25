package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ClassMethodNameMatchesFieldNameInspector extends BasePhpInspection {
    private static final String strProblemDescription = "There is a field with the same name, please give the method other name like is*, get*";

    @NotNull
    public String getShortName() {
        return "ClassMethodNameMatchesFieldNameInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /** TODO: stick to class */
                PhpClass objClass = method.getContainingClass();
                String strMethodName = method.getName();
                if (null == objClass || StringUtil.isEmpty(strMethodName) || null == method.getNameIdentifier()) {
                    return;
                }

                for (Field objField : objClass.getFields()) {
                    if (objField.getName().equals(strMethodName)) {
                        holder.registerProblem(method.getNameIdentifier(), strProblemDescription, ProblemHighlightType.ERROR);
                        return;
                    }
                }
            }
        };
    }
}