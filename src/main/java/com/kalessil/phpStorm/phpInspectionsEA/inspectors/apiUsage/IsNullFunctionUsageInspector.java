package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class IsNullFunctionUsageInspector extends BasePhpInspection {
    private static final String messagePattern = "'%e%' construction should be used instead.";

    @NotNull
    public String getShortName() {
        return "IsNullFunctionUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* check parameters amount and name */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (1 != params.length || null == functionName || !functionName.equals("is_null")) {
                    return;
                }

                final PsiElement parent = reference.getParent();

                /* decide which message to use */
                boolean checksIsNull = true;
                PsiElement target    = reference;
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (null != operation && PhpTokenTypes.opNOT == operation.getNode().getElementType()) {
                        checksIsNull = false;
                        target       = parent;
                    }
                }
                if (parent instanceof BinaryExpression) {
                    /* extract expression parts */
                    final BinaryExpression expression = (BinaryExpression) parent;
                    PsiElement secondOperand          = expression.getLeftOperand();
                    if (reference == secondOperand) {
                        secondOperand = expression.getRightOperand();
                    }

                    if (PhpLanguageUtil.isBoolean(secondOperand)) {
                        target = parent;

                        final IElementType operation = expression.getOperationType();
                        if (PhpTokenTypes.opEQUAL == operation || PhpTokenTypes.opIDENTICAL == operation) {
                            checksIsNull = PhpLanguageUtil.isTrue(secondOperand);
                        } else if (PhpTokenTypes.opNOT_EQUAL == operation || PhpTokenTypes.opNOT_IDENTICAL == operation) {
                            checksIsNull = !PhpLanguageUtil.isTrue(secondOperand);
                        } else {
                            target = reference;
                        }
                    }
                }

                /* report the issue */
                final String replacement = (checksIsNull ? "null === " : "null !== ") + params[0].getText();
                final String message     = messagePattern.replace("%e%", replacement);
                holder.registerProblem(target, message, ProblemHighlightType.WEAK_WARNING, new CompareToNullFix(replacement));
            }
        };
    }

    class CompareToNullFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use null comparison instead";
        }

        CompareToNullFix(@NotNull String expression) {
            super(expression);
        }
    }
}