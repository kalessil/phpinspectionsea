package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class DeprecatedConstructorStyleInspector extends BasePhpInspection {
    private static final String strProblemDescription = "%s% has a deprecated constructor";

    @NotNull
    public String getShortName() {
        return "DeprecatedConstructorStyleInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                PhpClass objClass = method.getContainingClass();
                String strMethodName = method.getName();
                if (
                    null == objClass || objClass.isTrait() || objClass.isInterface() ||
                    StringUtil.isEmpty(strMethodName) || null == method.getNameIdentifier()) {
                    return;
                }

                String strClassName = objClass.getName();
                if (strMethodName.equals(strClassName)) {
                    String strMessage = strProblemDescription.replace("%s%", strClassName);
                    holder.registerProblem(method.getNameIdentifier(), strMessage, ProblemHighlightType.LIKE_DEPRECATED);
                }
            }
        };
    }
}
