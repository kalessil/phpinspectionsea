package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class SenselessProxyMethodInspector extends BasePhpInspection {
    private static final String strProblemDescription = "%s% can be dropped, as it just calls parent method";

    @NotNull
    public String getShortName() {
        return "SenselessProxyMethodInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                for (Method objMethod : clazz.getOwnMethods()) {
                    String methodName = objMethod.getName();
                    if (StringUtil.isEmpty(methodName) || null == objMethod.getNameIdentifier()) {
                        continue;
                    }

                    GroupStatement body = ExpressionSemanticUtil.getGroupStatement(objMethod);
                    /*
                        skip processing methods without body (interfaces, abstract and etc.) or
                        contains not expected amount of expressions
                     */
                    if (null == body || 1 != ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        continue;
                    }

                    PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(body);
                    if (null != lastStatement && lastStatement.getFirstChild() instanceof MethodReference) {
                        MethodReference reference = (MethodReference) lastStatement.getFirstChild();

                        String referenceVariable = reference.getFirstChild().getText().trim();
                        String referenceName = reference.getName();

                        if (
                            referenceVariable.equals("parent") &&
                            !StringUtil.isEmpty(referenceName) &&  referenceName.equals(methodName)
                        ) {
                            String strWarning = strProblemDescription.replace("%s%", objMethod.getName());
                            holder.registerProblem(objMethod.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }
}
