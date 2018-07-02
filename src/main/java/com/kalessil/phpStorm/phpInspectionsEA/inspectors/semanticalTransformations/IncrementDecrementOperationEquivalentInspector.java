package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IncrementDecrementOperationEquivalentInspector extends BasePhpInspection {
    private static final String patternMessage = "Can be safely replaced with '%e%'.";

    // Inspection options.
    public boolean PREFER_PREFIX_STYLE = true;
    public boolean PREFER_SUFFIX_STYLE = false;

    @NotNull
    public String getShortName() {
        return "IncrementDecrementOperationEquivalentInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* ensures we are not touching arrays only, not strings and not objects */
            private boolean isArrayAccessOrString(@Nullable PhpPsiElement variable) {
                if (variable instanceof ArrayAccessExpression) {
                    final PsiElement container = ((ArrayAccessExpression) variable).getValue();
                    if (container instanceof PhpTypedElement) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) container, holder.getProject());
                        if (resolved != null) {
                            final Set<String> types = new HashSet<>();
                            resolved.filterUnknown().getTypes().forEach(t -> types.add(Types.getType(t)));

                            final boolean isArray = types.contains(Types.strArray) && !types.contains(Types.strString);
                            types.clear();
                            return !isArray;
                        }
                    }
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
                            final String replacement
                                    = PREFER_PREFIX_STYLE ? ("++" + variable.getText()) : (variable.getText() + "++");
                            final String message = patternMessage.replace("%e%", replacement);
                            holder.registerProblem(expression, message, new UseIncrementFix(replacement));
                        }
                    } else if (operation == PhpTokenTypes.opMINUS_ASGN) {
                        if (value.getText().equals("1") && !isArrayAccessOrString(variable)) {
                            final String replacement
                                    = PREFER_PREFIX_STYLE ? ("--" + variable.getText()) : (variable.getText() + "--");
                            final String message = patternMessage.replace("%e%", replacement);
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
                            (leftOperand.getText().equals("1") && OpenapiEquivalenceUtil.areEqual(rightOperand, variable)) ||
                            (rightOperand.getText().equals("1") && OpenapiEquivalenceUtil.areEqual(leftOperand, variable))
                        ) {
                            if (!isArrayAccessOrString(variable)) {
                                final String replacement
                                        = PREFER_PREFIX_STYLE ? ("++" + variable.getText()) : (variable.getText() + "++");
                                final String message = patternMessage.replace("%e%", replacement);
                                holder.registerProblem(assignmentExpression, message, new UseIncrementFix(replacement));
                            }
                        }
                    } else if (operation == PhpTokenTypes.opMINUS) {
                        /* minus operation: operand position IS important */
                        if (
                            rightOperand.getText().equals("1") &&
                            OpenapiEquivalenceUtil.areEqual(leftOperand, variable) &&
                            !isArrayAccessOrString(variable)
                        ) {
                            final String replacement
                                    = PREFER_PREFIX_STYLE ? ("--" + variable.getText()) : (variable.getText() + "--");
                            final String message = patternMessage.replace("%e%", replacement);
                            holder.registerProblem(assignmentExpression, message, new UseDecrementFix(replacement));
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.delegateRadioCreation((radioComponent) -> {
            radioComponent.addOption("Fix with prefix operation", PREFER_PREFIX_STYLE, (isSelected) -> PREFER_PREFIX_STYLE = isSelected);
            radioComponent.addOption("Fix with suffix operation", PREFER_SUFFIX_STYLE, (isSelected) -> PREFER_SUFFIX_STYLE = isSelected);
        }));
    }


    private static final class UseIncrementFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use increment operation instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseIncrementFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class UseDecrementFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use decrement operation instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseDecrementFix(@NotNull String expression) {
            super(expression);
        }
    }
}
