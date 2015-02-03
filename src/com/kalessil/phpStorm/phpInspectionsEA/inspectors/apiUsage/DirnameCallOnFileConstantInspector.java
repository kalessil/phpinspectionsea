package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class DirnameCallOnFileConstantInspector extends BasePhpInspection {
    private static final String strProblemDescription = "__DIR__ shall be used instead";
    private static final String strFile = "__FILE__";
    private static final String strDirName = "dirname";

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /** check requirements */
                final PsiElement[] arrParams = reference.getParameters();
                final String strFunction = reference.getName();
                if (arrParams.length != 1 || StringUtil.isEmpty(strFunction)) {
                    return;
                }
                PsiElement objFirstParameter = arrParams[0];
                if (!(objFirstParameter instanceof ConstantReference)) {
                    return;
                }

                /** inspect given construct */
                String strConstant = ((ConstantReference) objFirstParameter).getName();
                if (!StringUtil.isEmpty(strConstant) && strConstant.equals(strFile) && strFunction.equals(strDirName)) {
                    holder.registerProblem(reference, strProblemDescription, ProblemHighlightType.LIKE_DEPRECATED);
                }
            }
        };
    }
}
