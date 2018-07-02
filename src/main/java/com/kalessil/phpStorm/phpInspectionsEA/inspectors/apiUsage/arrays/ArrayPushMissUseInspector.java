package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ArrayPushMissUseInspector extends BasePhpInspection {
    private static final String messageMisuse   = "'%s' here would be up to 2x faster.";
    private static final String messageUnneeded = "It seems that the index can be omitted at all.";

    @NotNull
    public String getShortName() {
        return "ArrayPushMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_push")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && OpenapiTypesUtil.isStatementImpl(reference.getParent())) {
                        PsiElement variadicCandidate = arguments[1].getPrevSibling();
                        if (variadicCandidate instanceof PsiWhiteSpace) {
                            variadicCandidate = variadicCandidate.getPrevSibling();
                        }
                        if (!OpenapiTypesUtil.is(variadicCandidate, PhpTokenTypes.opVARIADIC)) {
                            final String replacement = String.format("%s[] = %s", arguments[0].getText(), arguments[1].getText());
                            holder.registerProblem(
                                    reference,
                                    String.format(messageMisuse, replacement),
                                    new UseElementPushFix(replacement)
                            );
                        }
                    }
                }
            }

            @Override
            public void visitPhpArrayAccessExpression(@NotNull ArrayAccessExpression expression) {
                final PsiElement parent = expression.getParent();
                if (OpenapiTypesUtil.isAssignment(parent)) {
                    final PsiElement value = ((AssignmentExpression) parent).getValue();
                    if (value != expression) {
                        final ArrayIndex index = expression.getIndex();
                        if (index != null) {
                            final PsiElement candidate = index.getValue();
                            if (candidate != null && OpenapiTypesUtil.isFunctionReference(candidate)) {
                                final FunctionReference reference = (FunctionReference) candidate;
                                final String functionName         = reference.getName();
                                if (functionName != null && functionName.equals("count")) {
                                    final PsiElement[] arguments = reference.getParameters();
                                    if (arguments.length == 1) {
                                        final PsiElement container = expression.getValue();
                                        if (container != null && OpenapiEquivalenceUtil.areEqual(container, arguments[0])) {
                                            holder.registerProblem(
                                                    reference,
                                                    messageUnneeded,
                                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                            );
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

    private static final class UseElementPushFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use []= instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseElementPushFix(@NotNull String expression) {
            super(expression);
        }
    }
}
