package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class UnnecessaryParenthesesInspector extends BasePhpInspection {
    private static final String message = "Unnecessary parentheses.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryParenthesesInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpParenthesizedExpression(@NotNull ParenthesizedExpression expression) {
                if (holder.getFile().getName().endsWith(".blade.php")) {
                    /* syntax injection there is not done properly for elseif, causing false-positives */
                    return;
                }

                final PhpPsiElement argument = expression.getArgument();
                final PsiElement parent      = expression.getParent();
                if (null == argument || null == parent) {
                    return;
                }

                /*
                    this matrix mostly contains reasonable variants,
                    couple of them might be ambiguous, but let's keep logic simple
                */
                boolean knowsLegalCases = (
                    (
                        argument instanceof BinaryExpression   ||
                        argument instanceof TernaryExpression  ||
                        argument instanceof UnaryExpression    ||
                        argument instanceof AssignmentExpression
                    ) && (
                        parent instanceof BinaryExpression     ||
                        parent instanceof TernaryExpression    ||
                        parent instanceof UnaryExpression      ||
                        parent instanceof AssignmentExpression ||
                        parent instanceof PhpReturn
                    )
                );
                knowsLegalCases = knowsLegalCases ||
                    argument instanceof Include ||
                    parent instanceof PhpCase ||
                    parent instanceof PhpEchoStatement ||
                    parent instanceof PhpPrintExpression ||
                    (parent instanceof ParameterList && argument instanceof TernaryExpression) ||
                    (parent instanceof ArrayAccessExpression && argument instanceof UnaryExpression)
                ;

                if (!knowsLegalCases && parent instanceof MemberReference) {
                    if (argument instanceof NewExpression) {
                        /* (new ...)->...: allow method/property access on newly created objects */
                        knowsLegalCases = true;
                    } else if (argument instanceof UnaryExpression) {
                        /* (clone ...)->...: allow method/property access on cloned objects */
                        final PsiElement operator = ((UnaryExpression) argument).getOperation();
                        knowsLegalCases = null != operator && operator.getNode().getElementType() == PhpTokenTypes.kwCLONE;
                    } else if (argument instanceof BinaryExpression) {
                        /* ( ?? )->...: allow method/property access on null coallesing operator */
                        knowsLegalCases = ((BinaryExpression) argument).getOperationType() == PhpTokenTypes.opCOALESCE;
                    } else if (argument instanceof TernaryExpression) {
                        /* ( ?: )->...: allow method/property access on ternary operator */
                        knowsLegalCases = true;
                    } else if (argument instanceof AssignmentExpression) {
                        /* ( = )->...: allow method/property access on assigned variable */
                        knowsLegalCases = true;
                    }
                }

                /* (...->property)(...), (...->method())(...), (function(){})(...): allow callable/__invoke calls */
                if (
                    !knowsLegalCases && OpenapiTypesUtil.isFunctionReference(parent) &&
                    (
                        argument instanceof MemberReference ||
                        argument instanceof UnaryExpression ||
                        argument instanceof NewExpression ||
                        OpenapiTypesUtil.isLambda(argument)
                    )
                ) {
                    return;
                }

                if (!knowsLegalCases) {
                    holder.registerProblem(expression, message, new TheLocalFix());
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove the brackets";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ParenthesizedExpression) {
                PsiElement target      = expression;
                PsiElement replacement = ((ParenthesizedExpression) expression).getArgument();
                /* clone replacement is a special case */
                final PsiElement parent = expression.getParent();
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (operation != null && operation.getNode().getElementType() == PhpTokenTypes.kwCLONE) {
                        target             = parent;
                        final String clone = "clone " + replacement.getText();
                        replacement        = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, clone);
                    }
                }
                /* replace now */
                if (replacement != null) {
                    target.replace(replacement);
                }
            }
        }
    }
}
