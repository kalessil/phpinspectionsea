package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
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

public class ArrayIsListCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

    @NotNull
    @Override
    public String getShortName() {
        return "ArrayIsListCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'array_is_list(...)' can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("array_keys") || functionName.equals("array_values"))) {
                    final boolean isTargetVersion = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP800);
                    if (isTargetVersion) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1) {
                            final PsiElement context = reference.getParent();
                            if (context instanceof BinaryExpression) {
                                final BinaryExpression binary = (BinaryExpression) context;
                                final IElementType operator   = binary.getOperationType();
                                if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                                    final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                    if (functionName.equals("array_values")) {
                                        if (second != null && OpenapiEquivalenceUtil.areEqual(arguments[0], second)) {
                                            this.report(reference, operator, arguments[0]);
                                        }
                                    } else if (functionName.equals("array_keys")) {
                                        if (OpenapiTypesUtil.isFunctionReference(second)) {
                                            final FunctionReference rightReference = (FunctionReference) second;
                                            final String rightFunctionName         = rightReference.getName();
                                            final PsiElement[] rightArguments      = rightReference.getParameters();
                                            if ((rightFunctionName != null && rightFunctionName.equals("range")) && rightArguments.length == 2) {
                                                final boolean rangeFromZero = OpenapiTypesUtil.isNumber(rightArguments[0]) && rightArguments[0].getText().equals("0");
                                                if (rangeFromZero && rightArguments[1] instanceof BinaryExpression) {
                                                    final BinaryExpression rangeToBinary = (BinaryExpression) rightArguments[1];
                                                    if (rangeToBinary.getOperationType() == PhpTokenTypes.opMINUS) {
                                                        final PsiElement right = rangeToBinary.getRightOperand();
                                                        final PsiElement left  = rangeToBinary.getLeftOperand();
                                                        if (OpenapiTypesUtil.isNumber(right) && right.getText().equals("1") && OpenapiTypesUtil.isFunctionReference(left)) {
                                                            final FunctionReference leftReference = (FunctionReference) left;
                                                            final String leftName                 = leftReference.getName();
                                                            final PsiElement[] leftArguments      = leftReference.getParameters();
                                                            if (leftName != null && leftName.equals("count") && leftArguments.length == 1) {
                                                                if (OpenapiEquivalenceUtil.areEqual(arguments[0], leftArguments[0])) {
                                                                    this.report(reference, operator, arguments[0]);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void report(@NotNull FunctionReference reference, @NotNull IElementType operator, @NotNull PsiElement argument) {
                final boolean listExpected = operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opEQUAL;
                final String replacement   = String.format(
                        "%s%sarray_is_list(%s)",
                        listExpected ? "" : "!",
                        reference.getImmediateNamespaceName(),
                        argument.getText()
                );
                holder.registerProblem(
                        reference.getParent(),
                        String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                        new UseArrayIsListFix(replacement)
                );

            }
        };
    }

    private static final class UseArrayIsListFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'array_is_list(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseArrayIsListFix(@NotNull String expression) {
            super(expression);
        }
    }
}