package com.kalessil.phpStorm.phpInspectionsEA.inspectors.propel16;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                //PhpIndex.getInstance(holder.getProject()).getClassByName("Propel");

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

                //PhpIndex.getInstance(holder.getProject()).getClassByName("Propel");

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

                /** should contain .count even if counted with function */
                if (null != strMethodSuffix) {
                    strSignature += strMethodSuffix;
                }


                Pattern pattern = Pattern.compile(".+\\\\([\\w]+)\\.(get\\w+s)\\.count$");
                Matcher matcher = pattern.matcher(strSignature);

                /** find FKs collections usages */
                final boolean isCountOnCollectionViaFK =
                    strSignature.contains(".get") &&
                    matcher.matches()
                ;
                if (isCountOnCollectionViaFK) {
                    /** lookup class and method definition */
                    PhpClass objObjectClass = PhpIndex.getInstance(holder.getProject()).getClassByName(matcher.group(1));
                    if (null == objObjectClass) {
                        return;
                    }

                    String strMethodName = matcher.group(2);
                    for (Method objMethod: objObjectClass.getMethods()) {
                        if (!objMethod.getName().equals(strMethodName)) {
                            continue;
                        }

                        /** ensure propel generated method */
                        if (
                            objMethod.getParameters().length != 2 ||
                            !objMethod.getType().toString().contains("PropelObjectCollection")
                        ) {
                            return;
                        }
                    }

                    /** finally we are sure */
                    holder.registerProblem(objExpression, "FK collection count", ProblemHighlightType.LIKE_DEPRECATED);
                    return;
                }


                /** find search result collections usages */
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

