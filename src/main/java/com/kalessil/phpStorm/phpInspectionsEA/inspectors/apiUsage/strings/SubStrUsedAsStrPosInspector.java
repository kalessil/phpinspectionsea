package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SubStrUsedAsStrPosInspector extends BasePhpInspection {
    private static final String messagePattern = "'%r%' can be used instead (improves maintainability).";

    @NotNull
    public String getShortName() {
        return "SubStrUsedAsStrPosInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check if it's the target function: amount of parameters and name */
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (
                    (3 != arguments.length && 4 != arguments.length) || functionName == null ||
                    (!functionName.equals("substr") && !functionName.equals("mb_substr"))
                ) {
                    return;
                }

                /* checking 2nd and 3rd arguments is not needed/simplified:
                 *   - 2nd re-used as it is (should be a positive number!)
                 *   - 3rd is not important, as we'll rely on parent comparison operand instead
                 */
                final String index = arguments[1].getText();
                if (arguments[1].getNode().getElementType() != PhpElementTypes.NUMBER || !index.equals("0")) {
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
                if (OpenapiTypesUtil.isFunctionReference(parentExpression)) {
                    final FunctionReference parentCall = (FunctionReference) parentExpression;
                    final PsiElement[] parentArguments = parentCall.getParameters();
                    final String parentName            = parentCall.getName();
                    if (
                        parentName != null && parentArguments.length == 1 &&
                        (parentName.equals("strtoupper") || parentName.equals("strtolower"))
                    ) {
                        caseManipulated  = true;
                        highLevelCall    = parentExpression;
                        parentExpression = parentExpression.getParent();
                    }
                }

                /* check parent expression, to ensure pattern matched */
                if (parentExpression instanceof BinaryExpression) {
                    final BinaryExpression parent = (BinaryExpression) parentExpression;
                    if (PhpTokenTypes.tsCOMPARE_EQUALITY_OPS.contains(parent.getOperationType())) {
                        /* get second operand */
                        PsiElement secondOperand = parent.getLeftOperand();
                        if (secondOperand == highLevelCall) {
                            secondOperand = parent.getRightOperand();
                        }

                        final PsiElement operationNode = parent.getOperation();
                        if (secondOperand != null && operationNode != null) {
                            final String operator      = operationNode.getText();
                            final boolean isMbFunction = functionName.equals("mb_substr");
                            final boolean hasEncoding  = isMbFunction && 4 == arguments.length;
                            final String replacement   = "%i% %o% %f%(%s%, %p%%e%)"
                                .replace("%e%", hasEncoding ? (", " + arguments[3].getText()) : "")
                                .replace("%p%", secondOperand.getText())
                                .replace("%s%", arguments[0].getText())
                                .replace("%f%", (isMbFunction ? "mb_" : "") + (caseManipulated ? "stripos" : "strpos"))
                                .replace("%o%", operator.length() == 2 ? (operator + "=") : operator)
                                .replace("%i%", index);
                            final String message       = messagePattern.replace("%r%", replacement);
                            holder.registerProblem(parentExpression, message, new UseStringSearchFix(replacement));
                        }
                    }
                }
            }
        };
    }

    private class UseStringSearchFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use substring search instead";
        }

        UseStringSearchFix(@NotNull String expression) {
            super(expression);
        }
    }
}