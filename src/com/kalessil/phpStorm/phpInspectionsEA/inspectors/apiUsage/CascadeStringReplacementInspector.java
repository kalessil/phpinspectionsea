package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class CascadeStringReplacementInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This str_replace(...) call can be merged with previous one";

    @NotNull
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                /** try getting function reference, indicating pattern match */
                FunctionReference functionCall = getStrReplaceReference(assignmentExpression);
                if (null != functionCall) {
                    /* get previous non-comment expression */
                    PsiElement previous = assignmentExpression.getParent().getPrevSibling();
                    while (null != previous && !(previous instanceof PhpPsiElement)) {
                        previous = previous.getPrevSibling();
                    }


                    /** previous assignment shall be inspected, probably we can merge this one into it */
                    if (
                        null != previous &&
                        previous.getFirstChild() instanceof AssignmentExpression &&
                        null != getStrReplaceReference((AssignmentExpression) previous.getFirstChild())
                    ) {
                        /** ensure linking variable discoverable and call contains all params */
                        PsiElement [] params = functionCall.getParameters();
                        PsiElement objLinkingVariable = ((AssignmentExpression) previous.getFirstChild()).getVariable();
                        if (
                            objLinkingVariable instanceof Variable
                            && params.length == 3 && params[2] instanceof Variable
                        ) {
                            /** extract variable names from link points */
                            String strPreviousVariable = ((Variable) objLinkingVariable).getName();
                            String strCallSubject      = ((Variable) params[2]).getName();
                            if (
                                !StringUtil.isEmpty(strCallSubject) && !StringUtil.isEmpty(strPreviousVariable) &&
                                strCallSubject.equals(strPreviousVariable)
                            ) {
                                holder.registerProblem(functionCall, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                return;
                            }
                        }
                    }

                    /** TODO: 2nd parameter can contain array with identical replacement patterns - use it instead of array */
                }
            }
        };
    }

    private FunctionReference getStrReplaceReference(AssignmentExpression assignment) {
        PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignment.getValue());
        /** ensure function and not method reference */
        if (value instanceof FunctionReference && !(value instanceof MethodReference)) {
            String strFunction = ((FunctionReference) value).getName();
            /** is target function */
            if (!StringUtil.isEmpty(strFunction) && strFunction.equals("str_replace")) {
                return (FunctionReference) value;
            }
        }

        return null;
    }
}
