package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

public class InArrayCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "It's possible to use 'in_array(...)' here (reduces cognitive load).";

    @NotNull
    public String getShortName() {
        return "InArrayCanBeUsedInspection";
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

                    final List<BinaryExpression> conditions = this.filter(this.extract(expression, operator), operator);
                    if (conditions.size() > 1) {
                        /* `x !=[=] ... && x !=[=] ...`, `x ==[=] ... || x ==[=] ...` */
                        final Map<PsiElement, List<PsiElement>> groups = this.group(conditions);
                        for (final Map.Entry<PsiElement, List<PsiElement>> entry : groups.entrySet()) {
                            final List<PsiElement> values = entry.getValue();
                            if (values.size() > 1) {
                                /* if subject contains calls, report 2+ values, otherwise 3+ values */
                                final boolean isTarget = values.size() > 2 ||
                                                         PsiTreeUtil.findChildOfType(entry.getKey(), FunctionReference.class) != null;
                                if (isTarget) {
                                    holder.registerProblem(values.get(values.size() - 1).getParent(), message);
                                }
                            }
                            values.clear();
                        }
                        groups.clear();
                    }
                    conditions.clear();
                }
            }

            @NotNull
            private Map<PsiElement, List<PsiElement>> group(@NotNull List<BinaryExpression> conditions) {
                final HashMap<PsiElement, List<PsiElement>> result = new HashMap<>();
                for (final BinaryExpression binary : conditions) {
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    if (left != null && right != null) {
                        final List<PsiElement> values = Stream.of(left, right)
                                .filter(operand -> operand instanceof StringLiteralExpression ||
                                                   operand instanceof ConstantReference ||
                                                   operand instanceof ClassConstantReference ||
                                                   OpenapiTypesUtil.isNumber(operand)
                                )
                                .collect(Collectors.toList());
                        if (values.size() == 1) {
                            final PsiElement value   = values.get(0);
                            final PsiElement subject = OpenapiElementsUtil.getSecondOperand(binary, value);
                            final Optional<PsiElement> key = result.keySet().stream()
                                    .filter(s -> s != null && subject != null && OpenapiEquivalenceUtil.areEqual(s, subject))
                                    .findFirst();
                            if (!key.isPresent()) {
                                result.put(subject, new ArrayList<>(Collections.singletonList(value)));
                            } else {
                                result.get(key.get()).add(value);
                            }
                        }
                        /* alternatives as variables, fields array (as I understand currently) better to not report */
                    }
                }
                return result;
            }

            @NotNull
            private List<BinaryExpression> filter(@NotNull List<PsiElement> conditions, @Nullable IElementType operator) {
                final Set<IElementType> targetOperators = new HashSet<>();
                if (operator == PhpTokenTypes.opAND) {
                    targetOperators.add(PhpTokenTypes.opNOT_EQUAL);
                    targetOperators.add(PhpTokenTypes.opNOT_IDENTICAL);
                } else {
                    targetOperators.add(PhpTokenTypes.opEQUAL);
                    targetOperators.add(PhpTokenTypes.opIDENTICAL);
                }
                return conditions.stream()
                        .filter(b -> b instanceof BinaryExpression && targetOperators.contains(((BinaryExpression) b).getOperationType()))
                        .map(b    -> (BinaryExpression) b)
                        .collect(Collectors.toList());
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
