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
import org.jetbrains.annotations.Nullable;

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

public class UnnecessaryBolleanCheckInspector extends BasePhpInspection {
    @NotNull
    public String getShortName() {
        return "UnnecessaryContinueInspection";
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
                if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operation)) {
                    final List<PsiElement> booleans = Stream.of(expression.getLeftOperand(), expression.getRightOperand())
                            .filter(PhpLanguageUtil::isBoolean)
                            .collect(Collectors.toList());
                    if (booleans.size() == 1) {
                        final PsiElement bool   = booleans.get(0);
                        final PsiElement second = OpenapiElementsUtil.getSecondOperand(expression, bool);
                        if (second != null) {
                            /* extract value */
                            PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(second);
                            if (second instanceof UnaryExpression) {
                                final UnaryExpression unary = (UnaryExpression) second;
                                if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                                    value = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                                }
                            }
                            if (this.isBooleanOnly(value)) {

                            }
                        }
                    }
                    booleans.clear();
                }
            }

            private boolean isBooleanOnly(@Nullable PsiElement expression) {
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
