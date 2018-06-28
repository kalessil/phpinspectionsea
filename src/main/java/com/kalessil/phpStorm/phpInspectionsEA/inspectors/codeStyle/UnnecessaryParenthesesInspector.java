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

                /* expression.getArgument() returns the first child, but it can be more */
                final PsiElement[] children = expression.getChildren();
                final PsiElement argument   = children.length > 0 ? children[children.length - 1] : null;
                final PsiElement parent     = expression.getParent();
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
                    argument instanceof PhpYield ||
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
                        knowsLegalCases = OpenapiTypesUtil.is(((UnaryExpression) argument).getOperation(), PhpTokenTypes.kwCLONE);
                    } else if (argument instanceof BinaryExpression) {
                        /* ( ?? )->...: allow method/property access on null coallesing operator */
                        knowsLegalCases = ((BinaryExpression) argument).getOperationType() == PhpTokenTypes.opCOALESCE;
                    } else if (argument instanceof TernaryExpression) {
                        /* ( ?: )->...: allow method/property access on ternary operator */
                        knowsLegalCases = true;
                    } else if (argument instanceof AssignmentExpression) {
                        /* ( = )->...: allow method/property access on assigned variable */
                        knowsLegalCases = true;
                    } else if (OpenapiTypesUtil.isLambda(argument)) {
                        /* (function(){ ... })->call(...): allow invoking lambdas on objects */
                        knowsLegalCases = true;
                    }
                }

                /* (...->property)(...), (...->method())(...), (function(){})(...): allow callable/__invoke calls */
                if (
                    !knowsLegalCases && OpenapiTypesUtil.isFunctionReference(parent) &&
                    (
                        argument instanceof MemberReference ||
                        argument instanceof UnaryExpression ||
                        argument instanceof BinaryExpression ||
                        argument instanceof NewExpression ||
                        argument instanceof AssignmentExpression ||
                        OpenapiTypesUtil.isLambda(argument)
                    )
                ) {
                    return;
                }

                /* false negatives: certain cases needs re-evaluation ... */
                if (knowsLegalCases && argument instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) argument;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                        final PsiElement unaryArgument = unary.getValue();
                        if (unaryArgument instanceof PhpEmpty || unaryArgument instanceof PhpIsset) {
                            knowsLegalCases = false;
                        }
                    }
                }
                if (!knowsLegalCases) {
                    holder.registerProblem(expression, message, new TheLocalFix());
                }
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Remove the brackets";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ParenthesizedExpression && !project.isDisposed()) {
                PsiElement target      = expression;
                PsiElement replacement = ((ParenthesizedExpression) expression).getArgument();

                /* in some constructs QF can produce wrong results: deal with it */
                final PsiElement parent = expression.getParent();
                if (parent instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) parent).getOperation();
                    if (OpenapiTypesUtil.is(operation, PhpTokenTypes.kwCLONE)) {
                        target      = parent;
                        replacement = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, "clone " + replacement.getText());
                    }
                } else if (parent instanceof PhpContinue) {
                    target      = parent;
                    replacement = PhpPsiElementFactory.createFromText(project, PhpContinue.class, "continue " + replacement.getText() + ';');
                } else if (parent instanceof PhpBreak) {
                    target      = parent;
                    replacement = PhpPsiElementFactory.createFromText(project, PhpBreak.class, "break " + replacement.getText() + ';');
                }

                /* replace now */
                if (replacement != null) {
                    target.replace(replacement);
                }
            }
        }
    }
}
