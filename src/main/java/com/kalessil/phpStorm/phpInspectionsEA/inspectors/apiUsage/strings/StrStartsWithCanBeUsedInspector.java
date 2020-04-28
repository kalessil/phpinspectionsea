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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ReportingUtil;
import org.jetbrains.annotations.NotNull;

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
                final String functionName = reference.getName();
                if (functionName != null && (functionName.equals("strpos") || functionName.equals("mb_strpos"))) {
                    final boolean isAvailable = FunctionsPolyfillsIndexer.isFunctionAvailable("\\str_starts_with", holder.getProject());
                    if (isAvailable) {
                        final PsiElement[] arguments = reference.getParameters();
                        if (arguments.length == 2) {
                            final PsiElement context = reference.getParent();
                            if (context instanceof BinaryExpression) {
                                final BinaryExpression binary = (BinaryExpression) context;
                                final IElementType operation  = binary.getOperationType();
                                if (operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opIDENTICAL) {
                                    final PsiElement second = OpenapiElementsUtil.getSecondOperand(binary, reference);
                                    if (second != null && OpenapiTypesUtil.isNumber(second) && second.getText().equals("0")) {
                                        final String replacement = String.format(
                                                "%s%sstr_starts_with(%s, %s)",
                                                operation == PhpTokenTypes.opNOT_IDENTICAL ? "! " : "",
                                                reference.getImmediateNamespaceName(),
                                                arguments[0].getText(),
                                                arguments[1].getText()
                                        );
                                        holder.registerProblem(
                                                binary,
                                                String.format(ReportingUtil.wrapReportedMessage(message), replacement),
                                                new UseStrStartsWithFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseStrStartsWithFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use 'str_starts_with(...)' instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseStrStartsWithFix(@NotNull String expression) {
            super(expression);
        }
    }
}
