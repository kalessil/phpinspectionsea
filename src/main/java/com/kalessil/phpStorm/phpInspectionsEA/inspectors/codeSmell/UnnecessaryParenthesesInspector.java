package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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
            public void visitPhpParenthesizedExpression(ParenthesizedExpression expression) {
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
                    (parent instanceof ParameterList && argument instanceof TernaryExpression)
                ;

                final boolean isMemberReference = parent instanceof MethodReference || parent instanceof FieldReference;
                /* (new ...)->...: allow method/property access on newly created objects */
                if (!knowsLegalCases && isMemberReference && argument instanceof NewExpression) {
                    knowsLegalCases = true;
                }
                /* (clone ...)->...: allow method/property access on cloned objects */
                if (!knowsLegalCases && isMemberReference && argument instanceof UnaryExpression) {
                    final PsiElement operator = ((UnaryExpression) argument).getOperation();
                    knowsLegalCases = null != operator && PhpTokenTypes.kwCLONE == operator.getNode().getElementType();
                }
                /* ( ?? )->...: allow method/property access on null coallesing operator */
                if (!knowsLegalCases && isMemberReference && argument instanceof BinaryExpression) {
                    knowsLegalCases = PhpTokenTypes.opCOALESCE == ((BinaryExpression) argument).getOperationType();
                }
                /* ( ?: )->...: allow method/property access on ternary operator */
                if (!knowsLegalCases && isMemberReference && argument instanceof TernaryExpression) {
                    knowsLegalCases = true;
                }
                /* ( = )->...: allow method/property access on assigned variable */
                if (!knowsLegalCases && isMemberReference && argument instanceof AssignmentExpression) {
                    knowsLegalCases = true;
                }

                /* (...->property)(...), (...->method())(...), (function(){})(...): allow callable/__invoke calls */
                if (
                    !knowsLegalCases && OpenapiTypesUtil.isFunctionReference(parent) &&
                    (
                        argument instanceof FieldReference || argument instanceof MethodReference ||
                        argument instanceof UnaryExpression || argument instanceof NewExpression ||
                        OpenapiTypesUtil.isLambda(argument)
                    )
                ) {
                    return;
                }

                if (!knowsLegalCases) {
                    holder.registerProblem(expression, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
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
                expression.replace(((ParenthesizedExpression) expression).getArgument());
            }
        }
    }
}
