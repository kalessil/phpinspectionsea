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

import java.util.HashSet;

public class MissingParentConstructorCallInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Missing 'parent::%s(...);' call";
    private static final String strParent = "parent";

    private HashSet<String> functionsSet = null;
    private HashSet<String> getFunctionsSet() {
        if (null == functionsSet) {
            functionsSet = new HashSet<>();

            functionsSet.add("__construct");
            functionsSet.add("__clone");
        }

        return functionsSet;
    }

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
                /** construction requirements */
                String strMethodName = method.getName();
                if (StringUtil.isEmpty(strMethodName) || strMethodName.charAt(0) != '_') {
                    return;
                }
                GroupStatement objBody = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == objBody) {
                    return;
                }

                /** test method name and containing class */
                PhpClass objClassForIteration = method.getContainingClass();
                if (null == objClassForIteration || !getFunctionsSet().contains(strMethodName)) {
                    return;
                }

                /** find parent with protected/public method, if not overrides anything, terminate inspection */
                PhpClass objParentWithGivenMethod = null;
                for (PhpClass superClass : objClassForIteration.getSupers()) {
                    Method objMethod = superClass.findOwnMethodByName(strMethodName);
                    if (null != objMethod && !objMethod.getAccess().isPrivate()) {
                        objParentWithGivenMethod = superClass;
                        break;
                    }
                }
                if (null == objParentWithGivenMethod) {
                    return;
                }


                /** check body for parent function usages */
                boolean isParentFunctionUsed = false;
                for (PsiElement objStatement: objBody.getStatements()) {
                    /** skip non-method invocations */
                    if (!(objStatement instanceof Statement)) {
                        continue;
                    }
                    objStatement = ((Statement) objStatement).getFirstPsiChild();
                    if (!(objStatement instanceof MethodReference)) {
                        continue;
                    }

                    /** construction requirements */
                    MethodReference objCall = (MethodReference) objStatement;
                    PhpExpression objSubject = objCall.getClassReference();
                    String strCallMethodName = objCall.getName();
                    if (null == objSubject || null == strCallMethodName) {
                        continue;
                    }

                    /** check if parent method invocation */
                    if (strCallMethodName.equals(strMethodName) && objSubject.getText().equals(strParent)) {
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
