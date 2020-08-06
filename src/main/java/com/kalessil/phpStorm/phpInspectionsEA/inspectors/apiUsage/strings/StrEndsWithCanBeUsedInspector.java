package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.indexers.FunctionsPolyfillsIndexer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StrEndsWithCanBeUsedInspector extends PhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

    @NotNull
    @Override
    public String getShortName() {
        return "StrEndsWithCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'str_ends_with(...)' can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION)) { return; }

                final String functionName = reference.getName();
                if (functionName != null) {
                    if (functionName.equals("substr") || functionName.equals("mb_substr")) {
                        final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_ends_with", holder.getProject());
                        if (isAvailable) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 2) {
                                final PsiElement context = reference.getParent();
                                if (context instanceof BinaryExpression) {
                                    final BinaryExpression binary = (BinaryExpression) context;
                                    final IElementType operation  = binary.getOperationType();
                                    if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) {
                                        final PsiElement lengthArgument = this.extractLengthArgument(arguments[1]);
                                        final PsiElement second         = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                        if (second != null && lengthArgument != null && OpenapiEquivalenceUtil.areEqual(second, lengthArgument)) {
                                            final String replacement = String.format(
                                                    "%s%sstr_ends_with(%s, %s)",
                                                    operation == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                                    reference.getImmediateNamespaceName(),
                                                    arguments[0].getText(),
                                                    second.getText()
                                            );
                                            holder.registerProblem(
                                                    binary,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                                    new UseStrEndsWithFix(replacement)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    } else if (functionName.equals("strpos") || functionName.equals("mb_strpos")) {
                        final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_ends_with", holder.getProject());
                        if (isAvailable) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 3) {
                                final PsiElement context = reference.getParent();
                                if (context instanceof BinaryExpression) {
                                    final BinaryExpression binary = (BinaryExpression) context;
                                    final IElementType operation  = binary.getOperationType();
                                    if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) {
                                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                        if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("-1")) {
                                            final PsiElement lengthArgument = this.extractLengthArgument(arguments[2]);
                                            if (lengthArgument != null && OpenapiEquivalenceUtil.areEqual(lengthArgument, arguments[1])) {
                                                final String replacement = String.format(
                                                        "%s%sstr_ends_with(%s, %s)",
                                                        operation == PhpTokenTypes.opIDENTICAL ? "! " : "",
                                                        reference.getImmediateNamespaceName(),
                                                        arguments[0].getText(),
                                                        arguments[1].getText()
                                                );
                                                holder.registerProblem(
                                                        binary,
                                                        String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                                        new UseStrEndsWithFix(replacement)
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private PsiElement extractLengthArgument(@Nullable PsiElement expression) {
                if (expression instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) expression;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opMINUS)) {
                        final PsiElement argument = unary.getValue();
                        if (argument != null) {
                            final Set<PsiElement> possibleValues = PossibleValuesDiscoveryUtil.discover(argument);
                            if (possibleValues.size() == 1) {
                                final PsiElement candidate = possibleValues.iterator().next();
                                if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                                    final FunctionReference reference = (FunctionReference) candidate;
                                    final String functionName         = reference.getName();
                                    if (functionName != null && (functionName.equals("strlen") || functionName.equals("mb_strlen"))) {
                                        final PsiElement[] arguments = reference.getParameters();
                                        if (arguments.length == 1) {
                                            return arguments[0];
                                        }
                                    }
                                }
                            }
                            possibleValues.clear();
                        }
                    }
                }
                return null;
            }
        };
    }

    private static final class UseStrEndsWithFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'str_ends_wit(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseStrEndsWithFix(@NotNull String expression) {
            super(expression);
        }
    }
}
