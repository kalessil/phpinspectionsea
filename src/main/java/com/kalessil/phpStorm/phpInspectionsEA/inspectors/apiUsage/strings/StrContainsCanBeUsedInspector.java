package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

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
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StrContainsCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

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
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("strpos") || functionName.equals("mb_strpos"))) {
                    final boolean isTargetVersion = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP800);
                    if (isTargetVersion) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2) {
                            final PsiElement context = reference.getParent();
                            if (context instanceof BinaryExpression) {
                                final BinaryExpression binary = (BinaryExpression) context;
                                final IElementType operation  = binary.getOperationType();
                                final boolean isTarget        = (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) &&
                                                                PhpLanguageUtil.isFalse(OpenapiElementsUtil.getSecondOperand(binary, reference));
                                if (isTarget) {
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
