package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class InArrayCanBeUsedInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "InArrayMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                final IElementType operator = expression.getOperationType();
                if (operator == PhpTokenTypes.opAND || operator == PhpTokenTypes.opOR) {
                    /* false-positives: part of another condition */
                    final PsiElement parent  = expression.getParent();
                    final PsiElement context = parent instanceof ParenthesizedExpression ? parent.getParent() : parent;
                    if (context instanceof BinaryExpression && ((BinaryExpression) context).getOperationType() == operator) {
                        return;
                    }

                    final List<PsiElement> conditions = this.extract(expression, operator);
                    if (conditions.size() > 1) {

                    }
                    conditions.clear();
                }
            }

            @NotNull
            private List<PsiElement> extract(@NotNull BinaryExpression binary, @Nullable IElementType operator) {
                final List<PsiElement> result = new ArrayList<>();
                if (binary.getOperationType() == operator) {
                    Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                            .map(ExpressionSemanticUtil::getExpressionTroughParenthesis).filter(Objects::nonNull)
                            .forEach(expression -> {
                                if (expression instanceof BinaryExpression) {
                                    result.addAll(this.extract((BinaryExpression) expression, operator));
                                } else if (expression instanceof UnaryExpression) {
                                    final UnaryExpression unary = (UnaryExpression) expression;
                                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                        result.add(ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue()));
                                    } else {
                                        result.add(unary);
                                    }
                                } else {
                                    result.add(expression);
                                }
                            });
                } else {
                    result.add(binary);
                }
                return result;
            }
        };
    }
}
