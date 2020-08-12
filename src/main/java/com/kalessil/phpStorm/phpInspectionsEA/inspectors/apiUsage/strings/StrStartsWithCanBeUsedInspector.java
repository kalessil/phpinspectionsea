package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
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

public class StrStartsWithCanBeUsedInspector extends PhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

    @NotNull
    @Override
    public String getShortName() {
        return "StrStartsWithCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'str_starts_with(...)' can be used";
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
                        /* case: substr($haystack, 0, strlen($needle)) === $needle */
                        final BinaryExpression context = this.extractTargetContext(reference, 3);
                        if (context != null) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(context, reference);
                            if (second != null) {
                                final PsiElement[] arguments = reference.getParameters();
                                final PsiElement needle      = this.extractLengthArgument(arguments[2]);
                                final boolean isTargetLength = needle != null && OpenapiEquivalenceUtil.areEqual(needle, second);
                                final boolean isTargetOffset = OpenapiTypesUtil.isNumber(arguments[1]) && arguments[1].getText().equals("0");
                                if (isTargetOffset && isTargetLength) {
                                    final String replacement = String.format(
                                            "%s%sstr_starts_with(%s, %s)",
                                            context.getOperationType() == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                            reference.getImmediateNamespaceName(),
                                            arguments[0].getText(),
                                            needle.getText()
                                    );
                                    holder.registerProblem(
                                            context,
                                            String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                            new UseStrStartsWithFix(replacement)
                                    );
                                }
                            }
                        }
                    } else if (functionName.equals("strpos") || functionName.equals("mb_strpos")) {
                        /* case: strpos($haystack, $needle) === 0 */
                        final BinaryExpression context = this.extractTargetContext(reference, 2);
                        if (context != null) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(context, reference);
                            if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("0")) {
                                final PsiElement[] arguments = reference.getParameters();
                                final String replacement     = String.format(
                                        "%s%sstr_starts_with(%s, %s)",
                                        context.getOperationType() == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                        reference.getImmediateNamespaceName(),
                                        arguments[0].getText(),
                                        arguments[1].getText()
                                );
                                holder.registerProblem(
                                        context,
                                        String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                        new UseStrStartsWithFix(replacement)
                                );
                            }
                        }
                    }  else if (functionName.equals("substr_compare")) {
                        /* case: substr_compare($haystack, $needle, 0, strlen($needle)) === 0 */
                        final BinaryExpression context = this.extractTargetContext(reference, 4);
                        if (context != null) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(context, reference);
                            if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("0")) {
                                final PsiElement[] arguments = reference.getParameters();
                                final PsiElement needle      = this.extractLengthArgument(arguments[3]);
                                final boolean isTargetLength = needle != null && OpenapiEquivalenceUtil.areEqual(needle, arguments[1]);
                                final boolean isTargetOffset = OpenapiTypesUtil.isNumber(arguments[2]) && arguments[2].getText().equals("0");
                                if (isTargetOffset && isTargetLength) {
                                    final String replacement = String.format(
                                            "%s%sstr_starts_with(%s, %s)",
                                            context.getOperationType() == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                            reference.getImmediateNamespaceName(),
                                            arguments[0].getText(),
                                            needle.getText()
                                    );
                                    holder.registerProblem(
                                            context,
                                            String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                            new UseStrStartsWithFix(replacement)
                                    );
                                }
                            }
                        }
                    } else if (functionName.equals("strncmp")) {
                        /* case: strncmp($haystack, $needle, strlen($needle)) === 0 */
                        final BinaryExpression context = this.extractTargetContext(reference, 3);
                        if (context != null) {
                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(context, reference);
                            if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("0")) {
                                final PsiElement[] arguments = reference.getParameters();
                                final PsiElement needle      = this.extractLengthArgument(arguments[2]);
                                if (needle != null && OpenapiEquivalenceUtil.areEqual(needle, arguments[1])) {
                                    final String replacement = String.format(
                                            "%s%sstr_starts_with(%s, %s)",
                                            context.getOperationType() == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                            reference.getImmediateNamespaceName(),
                                            arguments[0].getText(),
                                            needle.getText()
                                    );
                                    holder.registerProblem(
                                            context,
                                            String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                            new UseStrStartsWithFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private BinaryExpression extractTargetContext(@NotNull FunctionReference reference, int argumentsCount) {
                if (reference.getParameters().length == argumentsCount) {
                    final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_starts_with", holder.getProject());
                    if (isAvailable) {
                        final PsiElement context = reference.getParent();
                        if (context instanceof BinaryExpression) {
                            final BinaryExpression binary = (BinaryExpression) context;
                            final IElementType operation  = binary.getOperationType();
                            if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) {
                                return binary;
                            }
                        }
                    }
                }
                return null;
            }

            @Nullable
            private PsiElement extractLengthArgument(@Nullable PsiElement expression) {
                if (expression != null) {
                    final Set<PsiElement> possibleValues = PossibleValuesDiscoveryUtil.discover(expression);
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
                return null;
            }
        };
    }

    private static final class UseStrStartsWithFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'str_starts_with(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseStrStartsWithFix(@NotNull String expression) {
            super(expression);
        }
    }
}
