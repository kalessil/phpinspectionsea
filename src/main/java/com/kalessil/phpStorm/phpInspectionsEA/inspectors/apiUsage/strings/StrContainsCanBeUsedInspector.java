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

public class StrContainsCanBeUsedInspector extends PhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

    private static final Set<IElementType> targetOperations = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opGREATER);
        targetOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        targetOperations.add(PhpTokenTypes.opLESS);
        targetOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "StrContainsCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'str_contains(...)' can be used";
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
                    if (functionName.equals("strpos") || functionName.equals("mb_strpos")) {
                        /* case: strpos($haystack, $needle) !== false */
                        final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_contains", holder.getProject());
                        if (isAvailable) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 2) {
                                final PsiElement context = reference.getParent();
                                if (context instanceof BinaryExpression) {
                                    final BinaryExpression binary = (BinaryExpression) context;
                                    final IElementType operation  = binary.getOperationType();
                                    if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) {
                                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                        if (PhpLanguageUtil.isFalse(second)) {
                                            final String replacement = String.format(
                                                    "%s%sstr_contains(%s, %s)",
                                                    operation == PhpTokenTypes.opIDENTICAL ? "! " : "",
                                                    reference.getImmediateNamespaceName(),
                                                    arguments[0].getText(),
                                                    arguments[1].getText()
                                            );
                                            holder.registerProblem(
                                                    binary,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                                    new UseStrContainsFix(replacement)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    } else if (functionName.equals("substr_count") || functionName.equals("mb_substr_count")) {
                        final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_contains", holder.getProject());
                        if (isAvailable) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length == 2) {
                                if (ExpressionSemanticUtil.isUsedAsLogicalOperand(reference)) {
                                    /* case: substr_count($haystack, $needle) in logical context */
                                    final String replacement = String.format(
                                            "%sstr_contains(%s, %s)",
                                            reference.getImmediateNamespaceName(),
                                            arguments[0].getText(),
                                            arguments[1].getText()
                                    );
                                    holder.registerProblem(
                                            reference,
                                            String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                            new UseStrContainsFix(replacement)
                                    );
                                } else {
                                    /* case: substr_count($haystack, $needle) === 0 */
                                    final PsiElement context = reference.getParent();
                                    if (context instanceof BinaryExpression) {
                                        final BinaryExpression binary = (BinaryExpression) context;
                                        final IElementType operation  = binary.getOperationType();
                                        if (targetOperations.contains(operation)) {
                                            final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                            if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("0")) {
                                                final String replacement = String.format(
                                                        "%s%sstr_contains(%s, %s)",
                                                        operation == PhpTokenTypes.opIDENTICAL ? "! " : "",
                                                        reference.getImmediateNamespaceName(),
                                                        arguments[0].getText(),
                                                        arguments[1].getText()
                                                );
                                                holder.registerProblem(
                                                        binary,
                                                        String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                                        new UseStrContainsFix(replacement)
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
        };
    }

    private static final class UseStrContainsFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'str_contains(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseStrContainsFix(@NotNull String expression) {
            super(expression);
        }
    }
}
