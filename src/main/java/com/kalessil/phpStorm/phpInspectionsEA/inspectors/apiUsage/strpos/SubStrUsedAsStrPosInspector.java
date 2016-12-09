package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos;

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
    private static final String messagePattern = "'%i% %o% %f%(%s%, %p%)' can be used instead (improves maintainability)";

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
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (
                    (3 != params.length && 4 != params.length) || StringUtil.isEmpty(functionName) ||
                    (!functionName.equals("substr") && !functionName.equals("mb_substr"))
                ) {
                    return;
                }

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
                                final String message = messagePattern
                                        .replace("%f%", caseManipulated ? "stripos" : "strpos")
                                        .replace("%i%", index)
                                        .replace("%o%", operator.length() == 2 ? operator + "=" : operator)
                                        .replace("%s%", params[0].getText())
                                        .replace("%p%", secondOperand.getText());
                                holder.registerProblem(parentExpression, message, ProblemHighlightType.WEAK_WARNING);

                                // return;
                            }
                        }
                    }
                }
            }
        };
    }
}