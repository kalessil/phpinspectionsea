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

public class CountOnPropelCollectionInspector extends BasePhpInspection {
    private static final String strProblemDescriptionFkCollection = "This type of count in some cases shall be replaced " +
            "with constructing query object and operating on it (if collection is not used of course)";
    private static final String strProblemDescriptionSearchCollection = "This type of count can be performed on query object";
    private static final String strProblemDescriptionPropelCollection = "This type of count shall be checked manually, " +
            "possibly collection is result of a query and count can be performed on that query";

    @NotNull
    public String getDisplayName() {
        return "Propel API: count on collections";
    }

    @NotNull
    public String getShortName() {
        return "CountOnPropelCollectionInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            private boolean hasPropel = false;
            private boolean hasPropelChecked = false;

            public void visitPhpMethodReference(MethodReference reference) {
                /** check if propel main class exists */
                /** TODO: shall I care about composer file? */
                if (!this.hasPropelChecked) {
                    hasPropel = (PhpIndex.getInstance(holder.getProject()).getClassesByName("Propel").size() > 0);
                }
                if (!hasPropel) {
                    return;
                }

                /** only count calls to check */
                String strName = reference.getName();
                if (null == strName || !strName.equals("count")) {
                    return;
                }

                this.inspectSignature(reference.getSignature(), reference, null);
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                /** check if propel main class exists */
                /** TODO: shall I care about composer file? */
                if (!this.hasPropelChecked) {
                    hasPropel = (PhpIndex.getInstance(holder.getProject()).getClassesByName("Propel").size() > 0);
                }
                if (!hasPropel) {
                    return;
                }

                /** only count calls with one parameter to check */
                String strName = reference.getName();
                PsiElement[] arrParameters = reference.getParameters();
                if (null == strName || arrParameters.length != 1 || !strName.equals("count")) {
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
                /** re-dispatch poly-variant signatures */
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

                /** 1st case: FKs collections usages */
                final boolean isCountOnCollectionViaFK = (strSignature.contains(".get") && matcher.matches());
                if (isCountOnCollectionViaFK) {
                    /** lookup class and method definition */
                    PhpClass objObjectClass = PhpIndex.getInstance(holder.getProject()).getClassByName(matcher.group(1));
                    if (null == objObjectClass) {
                        return;
                    }

                    String strMethodName = matcher.group(2);
                    /** lookup method, but base classes can be not generated yet */
                    for (Method objMethod: objObjectClass.getMethods()) {
                        if (!objMethod.getName().equals(strMethodName)) {
                            continue;
                        }

                        /** ensure propel generated method */
                        if (objMethod.getParameters().length != 2 || !objMethod.getType().toString().contains("PropelObjectCollection")) {
                            return;
                        }
                    }

                    /** finally we are sure */
                    holder.registerProblem(objExpression, strProblemDescriptionFkCollection, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }


                /** 2nd case: found results collections usages */
                final boolean isCountOnResults =
                    strSignature.contains(".create.") &&
                    strSignature.matches(".+\\.create(\\.\\w+)*\\.find(By\\w+)?(\\.toArray)?\\.count$")
                ;
                if (isCountOnResults) {
                    holder.registerProblem(objExpression, strProblemDescriptionSearchCollection, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }


                /** 3rd case - signature affected by types annotating */
                final boolean isCountOnCollection =
                    strSignature.contains("\\Propel") && strSignature.endsWith("Collection.count")
                ;
                if (isCountOnCollection) {
                    holder.registerProblem(objExpression, strProblemDescriptionPropelCollection, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }
            }
        };
    }
}

