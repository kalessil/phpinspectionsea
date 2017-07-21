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

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
                final FunctionReference functionCall = getStrReplaceReference(assignmentExpression);
                if (functionCall != null) {
                    final PsiElement[] params = functionCall.getParameters();
                    if (params.length == 3) {
                        /* case: cascading replacements */
                        final AssignmentExpression previous = this.getPreviousAssignment(assignmentExpression);
                        if (previous != null && getStrReplaceReference(previous) != null) {
                            final PsiElement transitionVariable = previous.getVariable();
                            if (transitionVariable instanceof Variable && params[2] instanceof Variable) {
                                /* ensure previous, used and result storage is the same variable */
                                final String previousVariableName  = ((Variable) transitionVariable).getName();
                                final String callSubjectName       = ((Variable) params[2]).getName();
                                final PsiElement callResultStorage = assignmentExpression.getVariable();
                                if (
                                    callResultStorage != null && callSubjectName.equals(previousVariableName) &&
                                    PsiEquivalenceUtil.areElementsEquivalent(transitionVariable, callResultStorage)
                                ) {
                                    holder.registerProblem(functionCall, messageCascading);
                                }
                            }
                        }

                        /* other cases */
                        this.checkNestedCalls(params[2]);
                        this.checkReplacementSimplification(params[1]);
                    }
                }
            }

            @Nullable
            private AssignmentExpression getPreviousAssignment(@NotNull AssignmentExpression assignmentExpression) {
                /* get previous non-comment, non-php-doc expression */
                PsiElement previous = assignmentExpression.getParent().getPrevSibling();
                while (previous != null && !(previous instanceof PhpPsiElement)) {
                    previous = previous.getPrevSibling();
                }
                while (previous instanceof PhpDocComment) {
                    previous = ((PhpDocComment) previous).getPrevPsiSibling();
                }
                /* grab the target assignment */
                final AssignmentExpression result;
                if (previous != null && previous.getFirstChild() instanceof AssignmentExpression) {
                    result = (AssignmentExpression) previous.getFirstChild();
                } else {
                    result = null;
                }
                return result;
            }

            private void checkReplacementSimplification(@NotNull PsiElement replacementExpression) {
                if (replacementExpression instanceof ArrayCreationExpression) {
                    final Set<String> replacements = new HashSet<>();
                    for (final PsiElement oneReplacement : replacementExpression.getChildren()) {
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
                        holder.registerProblem(replacementExpression, messageReplacements, ProblemHighlightType.WEAK_WARNING);
                    }
                    replacements.clear();
                }
            }

            private void checkNestedCalls(@NotNull PsiElement callCandidate) {
                if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                    /* ensure 3rd argument is nested call of str_replace */
                    final String functionName = ((FunctionReference) callCandidate).getName();
                    if (functionName != null && functionName.equals("str_replace")) {
                        holder.registerProblem(callCandidate, messageNesting);
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
