package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class MissingParentConstructorCallInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Missing 'parent::%s(...);' call";

    @NotNull
    public String getDisplayName() {
        return "Probable bugs: missing parent constructor/clone call";
    }

    @NotNull
    public String getShortName() {
        return "MissingParentConstructorCallInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                String strMethodName = method.getName();
                if (StringUtil.isEmpty(strMethodName) || strMethodName.charAt(0) != '_') {
                    return;
                }

                /** test method name and containing class */
                PhpClass objClassForIteration = method.getContainingClass();
                if (null == objClassForIteration) {
                    return;
                }
                final boolean isConstructor = strMethodName.equals("__construct");
                final boolean isClone = !isConstructor && strMethodName.equals("__clone");
                if (!isClone && !isConstructor) {
                    return;
                }

                /** find super class with given method */
                PhpClass objParentWithGivenMethod = null;
                boolean hasGivenMethod = false;
                objClassForIteration = objClassForIteration.getSuperClass();
                while (null != objClassForIteration) {
                    for (Method objMethod : objClassForIteration.getMethods()) {
                        if (objMethod.getName().equals(strMethodName)) {
                            hasGivenMethod = !(objMethod.getAccess().isPrivate());
                            break;
                        }
                    }

                    if (hasGivenMethod) {
                        objParentWithGivenMethod = objClassForIteration;
                        break;
                    }

                    objClassForIteration = objClassForIteration.getSuperClass();
                }
                /** not overrides anything, terminate inspection */
                if (null == objParentWithGivenMethod) {
                    return;
                }

                /** check body for parent function usages */
                GroupStatement objBody = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == objBody) {
                    return;
                }
                boolean isParentFunctionUsed = false;
                for (PsiElement objStatement: objBody.getStatements()) {
                    if (!(objStatement instanceof Statement)) {
                        continue;
                    }
                    objStatement = ((Statement) objStatement).getFirstPsiChild();
                    if (!(objStatement instanceof MethodReference)) {
                        continue;
                    }

                    MethodReference objCall = (MethodReference) objStatement;

                    PhpExpression objSubject = objCall.getClassReference();
                    String strCallMethodName = objCall.getName();
                    if (null == objSubject || null == strCallMethodName) {
                        continue;
                    }

                    if (strCallMethodName.equals(strMethodName) && objSubject.getText().equals("parent")) {
                        isParentFunctionUsed = true;
                        break;
                    }
                }

                if (!isParentFunctionUsed) {
                    holder.registerProblem(method.getNameIdentifier(), strProblemDescription.replace("%s", strMethodName), ProblemHighlightType.ERROR);
                }
            }
        };
    }
}
