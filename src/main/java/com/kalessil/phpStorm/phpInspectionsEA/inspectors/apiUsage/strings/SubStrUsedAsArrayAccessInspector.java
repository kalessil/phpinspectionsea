package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SubStrUsedAsArrayAccessInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' might be used instead (invalid index accesses might show up).";

    @NotNull
    @Override
    public String getShortName() {
        return "SubStrUsedAsArrayAccessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'substr(...)' used as index-based access";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* check if it's the target function */
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("substr")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 3) {
                        final PsiElement length = arguments[2];
                        if (OpenapiTypesUtil.isNumber(length) && length.getText().equals("1")) {
                            final boolean isValidSource = arguments[0] instanceof Variable ||
                                                          arguments[0] instanceof ArrayAccessExpression ||
                                                          arguments[0] instanceof FieldReference;
                            if (isValidSource) {
                                final PhpTypedElement container = (PhpTypedElement) arguments[0];
                                final PhpType resolvedType      = OpenapiResolveUtil.resolveType(container, holder.getProject());
                                if (resolvedType != null) {
                                    final boolean isValidType = resolvedType.filterUnknown().getTypes().stream()
                                            .anyMatch(t -> Types.getType(t).equals(Types.strString));
                                    if (isValidType) {
                                        final String source      = arguments[0].getText();
                                        final String offset      = arguments[1].getText();
                                        final String replacement = offset.startsWith("-")
                                                ? String.format("%s[strlen(%s) %s]", source, source, offset.replaceFirst("-", "- "))
                                                : String.format( "%s[%s]", source, offset);
                                        holder.registerProblem(
                                                reference,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                new TheLocalFix(replacement)
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

    private static final class TheLocalFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use array access";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        TheLocalFix(@NotNull String expression) {
            super(expression);
        }
    }
}
