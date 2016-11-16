package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class CascadeStringReplacementInspector extends BasePhpInspection {
    private static final String messageNesting      = "This str_replace(...) call can be merged with parent one";
    private static final String messageCascading    = "This str_replace(...) call can be merged with previous one";
    private static final String messageReplacements = "Can be replaced with the string duplicated in array";

    @NotNull
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                /* try getting function reference, indicating pattern match */
                final FunctionReference functionCall = getStrReplaceReference(assignmentExpression);
                if (null != functionCall) {
                    /* get previous non-comment, non-php-doc expression */
                    PsiElement previous = assignmentExpression.getParent().getPrevSibling();
                    while (null != previous && !(previous instanceof PhpPsiElement)) {
                        previous = previous.getPrevSibling();
                    }
                    while (previous instanceof PhpDocComment) {
                        previous = ((PhpDocComment) previous).getPrevPsiSibling();
                    }
                    final PsiElement[] params = functionCall.getParameters();


                    /* === cascade calls check === */
                    /* previous assignment shall be inspected, probably we can merge this one into it */
                    if (
                        null != previous && previous.getFirstChild() instanceof AssignmentExpression &&
                        null != getStrReplaceReference((AssignmentExpression) previous.getFirstChild())
                    ) {
                        /* ensure linking variable discoverable and call contains all params */
                        final PsiElement glueVariable = ((AssignmentExpression) previous.getFirstChild()).getVariable();
                        if (3 == params.length && glueVariable instanceof Variable && params[2] instanceof Variable) {
                            /* extract variable names from link points */
                            final String previousVariable = ((Variable) glueVariable).getName();
                            final String callSubject      = ((Variable) params[2]).getName();
                            if (
                                !StringUtil.isEmpty(callSubject) && !StringUtil.isEmpty(previousVariable) &&
                                callSubject.equals(previousVariable)
                            ) {
                                holder.registerProblem(functionCall, messageCascading, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }


                    /* === nested calls check === */
                    if (3 == params.length && params[2] instanceof FunctionReferenceImpl) {
                        /* ensure 3rd argument is nested call of str_replace */
                        final String functionName = ((FunctionReference) params[2]).getName();
                        if (!StringUtil.isEmpty(functionName) && functionName.equals("str_replace")) {
                            holder.registerProblem(params[2], messageNesting, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }


                    /* === replacements uniqueness check === */
                    if (3 == params.length && params[1] instanceof ArrayCreationExpression) {
                        final HashSet<String> replacements = new HashSet<>();

                        for (PsiElement oneReplacement : params[1].getChildren()) {
                            if (oneReplacement instanceof PhpPsiElement) {
                                final PhpPsiElement item = ((PhpPsiElement) oneReplacement).getFirstPsiChild();
                                /* abort on non-string entries  */
                                if (!(item instanceof StringLiteralExpression)) {
                                    return;
                                }

                                replacements.add(item.getText());
                            }
                        }

                        /* count unique replacements */
                        final int uniqueReplacements = replacements.size();
                        replacements.clear();

                        if (1 == uniqueReplacements) {
                            holder.registerProblem(params[1], messageReplacements, ProblemHighlightType.WEAK_WARNING);
                        }
                    }
                }
            }
        };
    }

    private FunctionReference getStrReplaceReference(AssignmentExpression assignment) {
        final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignment.getValue());
        /* ensure function and not method reference */
        if (value instanceof FunctionReferenceImpl) {
            final String functionName = ((FunctionReference) value).getName();
            /* is target function */
            if (!StringUtil.isEmpty(functionName) && functionName.equals("str_replace")) {
                return (FunctionReference) value;
            }
        }

        return null;
    }
}
