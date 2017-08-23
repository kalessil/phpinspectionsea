package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.TypeFromPlatformResolverUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PrefixedIncDecrementEquivalentInspector extends BasePhpInspection {
    private static final String patternMessage = "Can be safely replaced with '%e%'.";

    @NotNull
    public String getShortName() {
        return "PrefixedIncDecrementEquivalentInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* ensures we are not touching arrays only, not strings and not objects */
            private boolean isArrayAccessOrString(@Nullable PhpPsiElement variable) {
                if (variable instanceof ArrayAccessExpression) {
                    final HashSet<String> containerTypes = new HashSet<>();
                    TypeFromPlatformResolverUtil.resolveExpressionType(((ArrayAccessExpression) variable).getValue(), containerTypes);
                    boolean isArray = containerTypes.contains(Types.strArray) && !containerTypes.contains(Types.strString);

                    containerTypes.clear();
                    return !isArray;
                }

                return false;
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                final IElementType operation = expression.getOperationType();
                final PhpPsiElement value    = expression.getValue();
                final PhpPsiElement variable = expression.getVariable();
                if (null != value && null != operation && null != variable) {
                    if (operation == PhpTokenTypes.opPLUS_ASGN) {
                        if (value.getText().equals("1") && !isArrayAccessOrString(variable)) {
                            final String replacement = "++" + variable.getText();
                            final String message     = patternMessage.replace("%e%", replacement);
                            holder.registerProblem(expression, message, new UseIncrementFix(replacement));
                        }
                    } else if (operation == PhpTokenTypes.opMINUS_ASGN) {
                        if (value.getText().equals("1") && !isArrayAccessOrString(variable)) {
                            final String replacement = "--" + variable.getText();
                            final String message     = patternMessage.replace("%e%", replacement);
                            holder.registerProblem(expression, message, new UseDecrementFix(replacement));
                        }
                    }
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                final PhpPsiElement variable = assignmentExpression.getVariable();
                if (variable != null && assignmentExpression.getValue() instanceof BinaryExpression) {
                    final BinaryExpression value = (BinaryExpression) assignmentExpression.getValue();

                    /* operation and operands provided */
                    final PsiElement leftOperand  = value.getLeftOperand();
                    final PsiElement rightOperand = value.getRightOperand();
                    final IElementType operation  = value.getOperationType();
                    if (null == leftOperand || null == rightOperand || null == operation) {
                        return;
                    }

                    if (operation == PhpTokenTypes.opPLUS) {
                        /* plus operation: operand position NOT important */
                        if (
                            (leftOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(rightOperand, variable)) ||
                            (rightOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable))
                        ) {
                            if (!isArrayAccessOrString(variable)) {
                                final String replacement = "++" + variable.getText();
                                final String message     = patternMessage.replace("%e%", replacement);
                                holder.registerProblem(assignmentExpression, message, new UseIncrementFix(replacement));
                            }
                        }
                    } else if (operation == PhpTokenTypes.opMINUS) {
                        /* minus operation: operand position IS important */
                        if (
                            rightOperand.getText().equals("1") &&
                            PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable) &&
                            !isArrayAccessOrString(variable)
                        ) {
                            final String replacement = "--" + variable.getText();
                            final String message     = patternMessage.replace("%e%", replacement);
                            holder.registerProblem(assignmentExpression, message, new UseDecrementFix(replacement));
                        }
                    }
                }
            }
        };
    }

    private class UseIncrementFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use increment operation instead";
        }

        UseIncrementFix(@NotNull String expression) {
            super(expression);
        }
    }

    private class UseDecrementFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use decrement operation instead";
        }

        UseDecrementFix(@NotNull String expression) {
            super(expression);
        }
    }
}
