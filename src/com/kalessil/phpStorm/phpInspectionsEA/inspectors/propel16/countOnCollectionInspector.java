package com.kalessil.phpStorm.phpInspectionsEA.inspectors.propel16;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class countOnCollectionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This call is on propel collection";

    @NotNull
    public String getDisplayName() {
        return "Semantics: count on propel collection";
    }

    @NotNull
    public String getShortName() {
        return "countOnCollectionInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                /** only count calls to check */
                String strName = reference.getName();
                if (
                    null == strName ||
                    !strName.equals("count")
                ) {
                    return;
                }

                this.inspectSignature(reference.getSignature(), reference, null);
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                /** only count calls with one parameter to check */
                String strName = reference.getName();
                PsiElement[] arrParameters = reference.getParameters();
                if (
                    null == strName ||
                    arrParameters.length != 1 ||
                    !strName.equals("count")
                ) {
                    return;
                }

                PsiElement objParameter = ExpressionSemanticUtil.getExpressionTroughParenthesis(arrParameters[0]);
                if (null == objParameter) {
                    return;
                }

                /** handle different types of argument */
                if (objParameter instanceof MethodReference) {
                    this.inspectSignature(((MethodReference) objParameter).getSignature(), objParameter, ".count");
                }
                if (objParameter instanceof Variable) {
                    this.inspectSignature(((Variable) objParameter).getSignature(), objParameter, ".count");
                }
            }

            private void inspectSignature(String strSignature, PsiElement objExpression, String strMethodSuffix) {
                if (strSignature.contains("|")) {
                    for (String strOneSignature : strSignature.split("\\|")) {
                        this.inspectSignature(strOneSignature, objExpression, strMethodSuffix);
                    }
                    return;
                }

                /** should contain .count */
                if (null != strMethodSuffix) {
                    strSignature += strMethodSuffix;
                }

                final boolean isCountOnCollectionViaFK =
                    strSignature.contains(".get") &&
                    strSignature.matches(".+\\.get\\w+s\\.count$")
                ;
                if (isCountOnCollectionViaFK) {
                    holder.registerProblem(objExpression, "FK collection count", ProblemHighlightType.LIKE_DEPRECATED);
                    return;
                }

                final boolean isCountOnResults =
                    strSignature.contains(".create.") &&
                    strSignature.matches(".+\\.create(\\.\\w+)*\\.find(By\\w+)?(\\.toArray)?\\.count$")
                ;
                if (isCountOnResults) {
                    holder.registerProblem(objExpression, "Found collection count", ProblemHighlightType.LIKE_DEPRECATED);
                    return;
                }

                //?: #M#C\PropelCollection.count - expand if possible for scope
                //holder.registerProblem(objExpression, "?: " + strSignature, ProblemHighlightType.LIKE_DEPRECATED);
            }
        };
    }
}

