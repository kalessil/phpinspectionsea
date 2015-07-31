package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class SenselessProxyMethodInspector extends BasePhpInspection {
    private static final String strProblemDescription = "%s% can be dropped, as it only calls parent method";

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
                            Parameter[] methodParameters = objMethod.getParameters();

                            /* ensure no transformations happens */
                            boolean isDispatchingWithoutModifications = (reference.getParameters().length == methodParameters.length);
                            for (PsiElement argument: reference.getParameters()) {
                                if (!(argument instanceof Variable)) {
                                    isDispatchingWithoutModifications = false;
                                    break;
                                }
                            }

                            /* ensure no signature changes took place */
                            boolean isChangingSignature = false;
                            PsiReference referenceToMethod = reference.getReference();
                            if (null != referenceToMethod && isDispatchingWithoutModifications && methodParameters.length > 0){
                                PsiElement referenceResolved = referenceToMethod.resolve();
                                if (referenceResolved instanceof Method) {
                                    Parameter[] parentParameters = ((Method) referenceResolved).getParameters();

                                    for (int index = 0; index < parentParameters.length; ++index) {
                                        if (!PsiEquivalenceUtil.areElementsEquivalent(parentParameters[index], methodParameters[index])) {
                                            isChangingSignature = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            /* decide if need to report any issues */
                            if (isDispatchingWithoutModifications && !isChangingSignature) {
                                String strWarning = strProblemDescription.replace("%s%", objMethod.getName());
                                holder.registerProblem(objMethod.getNameIdentifier(), strWarning, ProblemHighlightType.WEAK_WARNING);
                            }
                        }
                    }
                }
            }
        };
    }
}
