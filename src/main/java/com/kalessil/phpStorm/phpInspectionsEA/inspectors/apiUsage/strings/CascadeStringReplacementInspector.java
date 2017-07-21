package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CascadeStringReplacementInspector extends BasePhpInspection {
    private static final String messageNesting      = "This str_replace(...) call can be merged with its parent.";
    private static final String messageCascading    = "This str_replace(...) call can be merged with the previous.";
    private static final String messageReplacements = "Can be replaced with the string duplicated in array.";

    @NotNull
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                /* try getting function reference, indicating pattern match */
                final FunctionReference functionCall = getStrReplaceReference(assignmentExpression);
                if (functionCall != null) {
                    /* get previous non-comment, non-php-doc expression */
                    PsiElement previous = assignmentExpression.getParent().getPrevSibling();
                    while (null != previous && !(previous instanceof PhpPsiElement)) {
                        previous = previous.getPrevSibling();
                    }
                    while (previous instanceof PhpDocComment) {
                        previous = ((PhpDocComment) previous).getPrevPsiSibling();
                    }
                    final PsiElement[] params = functionCall.getParameters();

                    /* TODO: dedicated method */
                    /* === cascade calls check === */
                    /* previous assignment should be inspected, probably we can merge this one into it */
                    if (
                        null != previous && previous.getFirstChild() instanceof AssignmentExpression &&
                        null != getStrReplaceReference((AssignmentExpression) previous.getFirstChild())
                    ) {
                        /* ensure linking variable discoverable and call contains all params */
                        final PsiElement glueVariable = ((AssignmentExpression) previous.getFirstChild()).getVariable();
                        if (3 == params.length && glueVariable instanceof Variable && params[2] instanceof Variable) {
                            /* ensure previous, used and result storage is the same variable */
                            final String previousVariableName  = ((Variable) glueVariable).getName();
                            final String callSubjectName       = ((Variable) params[2]).getName();
                            final PsiElement callResultStorage = assignmentExpression.getVariable();
                            if (
                                callResultStorage != null && callSubjectName.equals(previousVariableName) &&
                                PsiEquivalenceUtil.areElementsEquivalent(glueVariable, callResultStorage)
                            ) {
                                holder.registerProblem(functionCall, messageCascading);
                            }
                        }
                    }


                    /* TODO: dedicated method */
                    /* === nested calls check === */
                    if (params.length == 3 && OpenapiTypesUtil.isFunctionReference(params[2])) {
                        /* ensure 3rd argument is nested call of str_replace */
                        final String functionName = ((FunctionReference) params[2]).getName();
                        if (functionName != null && functionName.equals("str_replace")) {
                            holder.registerProblem(params[2], messageNesting);
                        }
                    }

                    /* TODO: dedicated method */
                    /* === replacements uniqueness check === */
                    if (params.length == 3 && params[1] instanceof ArrayCreationExpression) {
                        final Set<String> replacements = new HashSet<>();
                        for (final PsiElement oneReplacement : params[1].getChildren()) {
                            if (oneReplacement instanceof PhpPsiElement) {
                                final PhpPsiElement item = ((PhpPsiElement) oneReplacement).getFirstPsiChild();
                                /* abort on non-string entries  */
                                if (!(item instanceof StringLiteralExpression)) {
                                    return;
                                }
                                replacements.add(item.getText());
                            }
                        }
                        if (replacements.size() == 1) {
                            holder.registerProblem(params[1], messageReplacements, ProblemHighlightType.WEAK_WARNING);
                        }
                        replacements.clear();
                    }
                }
            }
        };
    }

    @Nullable
    private FunctionReference getStrReplaceReference(@NotNull AssignmentExpression assignment) {
        FunctionReference result = null;
        final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignment.getValue());
        if (OpenapiTypesUtil.isFunctionReference(value)) {
            final String functionName = ((FunctionReference) value).getName();
            if (functionName != null && functionName.equals("str_replace")) {
                result = (FunctionReference) value;
            }
        }
        return result;
    }
}
