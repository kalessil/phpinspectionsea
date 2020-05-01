package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
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

public class GetDebugTypeCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "Can be replaced by '%s' (improves maintainability).";

    @NotNull
    @Override
    public String getShortName() {
        return "GetDebugTypeCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'get_debug_type(...)' can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression ternary) {
                if (! ternary.isShort()) {
                    final PsiElement condition = ternary.getCondition();
                    if (OpenapiTypesUtil.isFunctionReference(condition)) {
                        final FunctionReference reference = (FunctionReference) condition;
                        final String functionName         = reference.getName();
                        if (functionName != null && functionName.equals("is_object")) {
                            final boolean isTargetVersion = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP800);
                            if (isTargetVersion) {
                                final PsiElement[] arguments = reference.getParameters();
                                if (arguments.length == 1 && arguments[0] != null) {
                                    final PsiElement positive = ternary.getTrueVariant();
                                    final PsiElement negative = ternary.getFalseVariant();
                                    if (positive != null && negative != null) {
                                        final boolean isTarget = this.is(positive, "get_class", arguments[0]) &&
                                                                 this.is(negative, "gettype", arguments[0]);
                                        if (isTarget && this.isFromRootNamespace(reference)) {
                                            final String replacement = String.format(
                                                    "%sget_debug_type(%s)",
                                                    reference.getImmediateNamespaceName(),
                                                    arguments[0].getText()
                                            );
                                            holder.registerProblem(
                                                    ternary,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(message), replacement),
                                                    new UseGetDebugTypeFix(replacement)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean is(
                    @NotNull PsiElement candidate,
                    @NotNull String targetName,
                    @NotNull PsiElement targetArgument
            ) {
                if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                    final FunctionReference reference = (FunctionReference) candidate;
                    final String functionName         = reference.getName();
                    if (functionName != null && functionName.equals(targetName)) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 1 && arguments[0] != null) {
                            return OpenapiEquivalenceUtil.areEqual(arguments[0], targetArgument) &&
                                   this.isFromRootNamespace(reference);
                        }
                    }
                }
                return false;
            }
        };
    }

    private static final class UseGetDebugTypeFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'get_debug_type(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseGetDebugTypeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
