package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UnnecessaryBooleanCheckInspector extends BasePhpInspection {
    private static final String message = "'%s' would fit better here (reduces cognitive load).";

    @NotNull
    public String getShortName() {
        return "UnnecessaryBooleanCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                final IElementType operation = expression.getOperationType();
                if (operation == PhpTokenTypes.opIDENTICAL || operation == PhpTokenTypes.opNOT_IDENTICAL) {
                    final List<PsiElement> booleans = Stream.of(expression.getLeftOperand(), expression.getRightOperand())
                            .filter(PhpLanguageUtil::isBoolean)
                            .collect(Collectors.toList());
                    if (booleans.size() == 1) {
                        final PsiElement bool   = booleans.get(0);
                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(expression, bool);
                        if (second != null) {
                            /* extract value */
                            boolean isValueInverted = false;
                            PsiElement value        = ExpressionSemanticUtil.getExpressionTroughParenthesis(second);
                            if (second instanceof UnaryExpression) {
                                final UnaryExpression unary = (UnaryExpression) second;
                                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                    value           = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                                    isValueInverted = true;
                                }
                            }
                            if (value != null && this.isBooleanOnly(value)) {
                                if (isValueInverted) {
                                    final boolean isClassicStyle = expression.getRightOperand() == bool;
                                    final List<String> parts     = new ArrayList<>();
                                    parts.add(isClassicStyle ? value.getText() : bool.getText());
                                    parts.add(operation == PhpTokenTypes.opIDENTICAL ? "!==" : "===");
                                    parts.add(isClassicStyle ? bool.getText() : value.getText());
                                    holder.registerProblem(expression, String.format(message, String.join(" ", parts)));
                                } else {
                                    final boolean isTarget = (operation == PhpTokenTypes.opNOT_IDENTICAL && PhpLanguageUtil.isFalse(bool)) ||
                                                             (operation == PhpTokenTypes.opIDENTICAL && PhpLanguageUtil.isTrue(bool));
                                    if (isTarget) {
                                        holder.registerProblem(expression, String.format(message, value.getText()));
                                    }
                                }
                            }
                        }
                    }
                    booleans.clear();
                }
            }

            private boolean isBooleanOnly(@NotNull PsiElement expression) {
                if (expression instanceof PhpTypedElement) {
                    final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, holder.getProject());
                    if (resolved != null) {
                        return resolved.size() == 1 &&
                               !resolved.hasUnknown() &&
                               resolved.getTypes().stream().allMatch(t -> Types.getType(t).equals(Types.strBoolean));
                    }
                }
                return false;
            }
        };
    }
}
