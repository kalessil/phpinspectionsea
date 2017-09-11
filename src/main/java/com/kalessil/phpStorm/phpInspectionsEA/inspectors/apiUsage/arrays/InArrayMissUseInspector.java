package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class InArrayMissUseInspector extends BasePhpInspection {
    private static final String patternComparison = "'%e%' should be used instead.";
    private static final String patternKeyExists  = "'%e%' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only."; // NOTE-TR: It cannot be anything else...

    @NotNull
    public String getShortName() {
        return "InArrayMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general structure requirements */
                final PsiElement[] params = reference.getParameters();
                final String functionName = reference.getName();
                if ((2 != params.length && 3 != params.length) || StringUtils.isEmpty(functionName) || !functionName.equals("in_array")) {
                    return;
                }

                /* Pattern: array_key_exists equivalence */
                if (params[1] instanceof FunctionReference) {
                    final FunctionReference subcall = (FunctionReference) params[1];
                    final String subcallName        = subcall.getName();
                    if (null != subcallName && subcallName.equals("array_keys")) {
                        /* ensure the subcall is a complete expression */
                        final PsiElement[] subcallParams = subcall.getParameters();
                        if (1 != subcallParams.length) {
                            return;
                        }

                        final String replacement = "array_key_exists(%k%, %a%)"
                            .replace("%a%", subcallParams[0].getText())
                            .replace("%k%", params[0].getText());
                        final String message     = patternKeyExists.replace("%e%", replacement);
                        holder.registerProblem(reference, message, new UseArrayKeyExistsFix(replacement));

                        return;
                    }
                }

                /* Pattern: comparison equivalence */
                if (params[1] instanceof ArrayCreationExpression) {
                    int itemsCount      = 0;
                    PsiElement lastItem = null;
                    for (PsiElement oneItem : params[1].getChildren()) {
                        if (oneItem instanceof PhpPsiElement) {
                            ++itemsCount;
                            lastItem = oneItem;
                        }
                    }

                    lastItem = lastItem instanceof ArrayHashElement ? ((ArrayHashElement) lastItem).getValue() : lastItem;
                    if (itemsCount <= 1 && null != lastItem) {
                        final PsiElement parent   = reference.getParent();

                        /* find out what what intended to happen */
                        boolean checkExists = true;
                        PsiElement target   = reference;
                        if (parent instanceof UnaryExpression) {
                            final PsiElement operation = ((UnaryExpression) parent).getOperation();
                            if (null != operation && PhpTokenTypes.opNOT == operation.getNode().getElementType()) {
                                checkExists = false;
                                target      = parent;
                            }
                        }
                        if (parent instanceof BinaryExpression) {
                            /* extract in_arrays' expression parts */
                            final BinaryExpression expression = (BinaryExpression) parent;
                            PsiElement secondOperand          = expression.getLeftOperand();
                            if (reference == secondOperand) {
                                secondOperand = expression.getRightOperand();
                            }

                            if (PhpLanguageUtil.isBoolean(secondOperand)) {
                                final IElementType operation = expression.getOperationType();
                                if (PhpTokenTypes.opEQUAL == operation || PhpTokenTypes.opIDENTICAL == operation) {
                                    target      = parent;
                                    checkExists = PhpLanguageUtil.isTrue(secondOperand);
                                } else if (PhpTokenTypes.opNOT_EQUAL == operation || PhpTokenTypes.opNOT_IDENTICAL == operation) {
                                    target      = parent;
                                    checkExists = !PhpLanguageUtil.isTrue(secondOperand);
                                } else {
                                    target = reference;
                                }
                            }
                        }

                        final boolean isStrict   = 3 == params.length && PhpLanguageUtil.isTrue(params[2]);
                        final String replacement = "%l% %o% %r%"
                                .replace("%r%", params[0].getText())
                                .replace("%o%", (checkExists ? "==" : "!=") + (isStrict ? "=" : ""))
                                .replace("%l%", lastItem.getText());
                        final String message     = patternComparison.replace("%e%", replacement);
                        holder.registerProblem(target, message, new UseComparisonFix(replacement));
                        // return;
                    }
                }
            }
        };
    }

    private class UseArrayKeyExistsFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use array_key_exists() instead";
        }

        UseArrayKeyExistsFix(@NotNull String expression) {
            super(expression);
        }
    }

    private class UseComparisonFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Compare elements instead";
        }

        UseComparisonFix(@NotNull String expression) {
            super(expression);
        }
    }
}
