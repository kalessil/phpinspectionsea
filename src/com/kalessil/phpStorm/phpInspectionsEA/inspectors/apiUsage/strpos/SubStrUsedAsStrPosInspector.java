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
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SubStrUsedAsStrPosInspector  extends BasePhpInspection {
    private static final String strProblemDescription = "'%i% %o% strpos(%s%, %p%)' shall be used instead";

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
                if (
                    params.length != 3 ||
                    StringUtil.isEmpty(strFunctionName) || !strFunctionName.equals("substr")
                ) {
                    return;
                }

                /* checking 2nd and 3rd arguments is not needed:
                 *   - 2nd re-used as it is
                 *   - 3rd is not important, as we'll rely on parent comparison operand instead
                 */

                /* check parent expression */
                if (reference.getParent() instanceof BinaryExpression) {
                    final BinaryExpression parent = (BinaryExpression) reference.getParent();
                    final PsiElement operation = parent.getOperation();
                    if (null != operation && null != operation.getNode()) {
                        final IElementType operationType = operation.getNode().getElementType();
                        if (
                            operationType == PhpTokenTypes.opIDENTICAL || operationType == PhpTokenTypes.opNOT_IDENTICAL ||
                            operationType == PhpTokenTypes.opEQUAL     || operationType == PhpTokenTypes.opNOT_EQUAL
                        ) {
                            /* get second operand */
                            PsiElement secondOperand = parent.getLeftOperand();
                            if (secondOperand == reference) {
                                secondOperand = parent.getRightOperand();
                            }

                            if (null != secondOperand) {
                                final String operator = operation.getText();
                                String message = strProblemDescription
                                        .replace("%i%", params[1].getText())
                                        .replace("%o%", operator.length() == 2 ? operator + "=" : operator)
                                        .replace("%s%", params[0].getText())
                                        .replace("%p%", secondOperand.getText());
                                holder.registerProblem(parent, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                                // return;
                            }
                        }
                    }
                }
            }
        };
    }
}
