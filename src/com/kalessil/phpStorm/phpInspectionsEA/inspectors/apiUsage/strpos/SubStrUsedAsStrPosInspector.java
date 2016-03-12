package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrUsedAsStrPosInspector extends BasePhpInspection {
    private static final String strProblemUseStrpos      = "'%i% %o% %f%(%s%, %p%)' should be used instead";
    private static final String strProblemDropLength     = "'%l%' can be safely dropped";
    private static final String strProblemSimplifyLength = "'%l%' can be safely dropped, so '-%r%' is only left";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsStrPosInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check if it's the target function: amount of parameters and name */
                final String strFunctionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (3 != params.length || StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("substr")) {
                    return;
                }

                /* Additional check: 3rd argument is "strlen($search) - strlen(...)"
                 *  - "strlen($search)" is not needed
                 */
                checkAmbiguousStrlenInThirdArgument(reference);

                /* checking 2nd and 3rd arguments is not needed/simplified:
                 *   - 2nd re-used as it is (should be a positive number!)
                 *   - 3rd is not important, as we'll rely on parent comparison operand instead
                 */
                final String index = params[1].getText();
                if (!(params[1] instanceof PhpExpressionImpl) || !index.trim().equals("0")) {
                    return;
                }

                /* prepare variables, so we could properly process polymorphic pattern */
                PsiElement highLevelCall    = reference;
                PsiElement parentExpression = reference.getParent();
                if (parentExpression instanceof ParameterList) {
                    parentExpression = parentExpression.getParent();
                }

                /* if the call wrapped with case manipulation, propose to use stripos */
                boolean caseManipulated = false;
                if (
                    parentExpression instanceof FunctionReferenceImpl &&
                    1 == ((FunctionReference) parentExpression).getParameters().length
                ) {
                    final String parentName = ((FunctionReference) parentExpression).getName();
                    if (!StringUtil.isEmpty(parentName) && (parentName.equals("strtoupper") || parentName.equals("strtolower"))) {
                        caseManipulated  = true;
                        highLevelCall    = parentExpression;
                        parentExpression = parentExpression.getParent();
                    }
                }

                /* check parent expression, to ensure pattern matched */
                if (parentExpression instanceof BinaryExpression) {
                    final BinaryExpression parent = (BinaryExpression) parentExpression;
                    final PsiElement operation    = parent.getOperation();
                    if (null != operation && null != operation.getNode()) {
                        final IElementType operationType = operation.getNode().getElementType();
                        if (
                            operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL ||
                            operationType == PhpTokenTypes.opEQUAL     || operationType == PhpTokenTypes.opNOT_EQUAL
                        ) {
                            /* get second operand */
                            PsiElement secondOperand = parent.getLeftOperand();
                            if (secondOperand == highLevelCall) {
                                secondOperand = parent.getRightOperand();
                            }

                            if (null != secondOperand) {
                                final String operator = operation.getText();
                                final String message = strProblemUseStrpos
                                        .replace("%f%", caseManipulated ? "stripos" : "strpos")
                                        .replace("%i%", index)
                                        .replace("%o%", operator.length() == 2 ? operator + "=" : operator)
                                        .replace("%s%", params[0].getText())
                                        .replace("%p%", secondOperand.getText());
                                holder.registerProblem(parentExpression, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                                // return;
                            }
                        }
                    }
                }
            }

            private void checkAmbiguousStrlenInThirdArgument(FunctionReference reference) {
                final PsiElement[] params = reference.getParameters();
                if (params[2] instanceof BinaryExpression) {
                    final BinaryExpression candidate = (BinaryExpression) params[2];

                    final PsiElement operation = candidate.getOperation();
                    if (null != operation && null != operation.getNode()) {
                        /* should be "* - *" */
                        final IElementType operationType = operation.getNode().getElementType();
                        if (operationType != PhpTokenTypes.opMINUS) {
                            return;
                        }

                        /* should be "strlen($search) - *" */
                        if (
                            candidate.getLeftOperand() instanceof FunctionReferenceImpl &&
                            null != candidate.getRightOperand()
                        ) {
                            final FunctionReference leftCall = (FunctionReference) candidate.getLeftOperand();
                            final String leftCallName = leftCall.getName();
                            final PsiElement[] leftCallParams = leftCall.getParameters();
                            if (
                                1 == leftCallParams.length && !StringUtil.isEmpty(leftCallName) && leftCallName.equals("strlen") &&
                                PsiEquivalenceUtil.areElementsEquivalent(leftCallParams[0], params[0])
                            ) {
                                if (PsiEquivalenceUtil.areElementsEquivalent(candidate.getRightOperand(), params[1])) {
                                    /* 3rd parameter not needed at all */
                                    final String message = strProblemDropLength.replace("%l%", params[2].getText());
                                    holder.registerProblem(params[2], message, ProblemHighlightType.LIKE_DEPRECATED);
                                } else {
                                    /* 3rd parameter can be simplified */
                                    final String message = strProblemSimplifyLength
                                            .replace("%l%", leftCall.getText())
                                            .replace("%r%", candidate.getRightOperand().getText());
                                    holder.registerProblem(leftCall, message, ProblemHighlightType.LIKE_DEPRECATED);
                                }

                                // return;
                            }
                        }
                    }
                }
            }
        };
    }
}