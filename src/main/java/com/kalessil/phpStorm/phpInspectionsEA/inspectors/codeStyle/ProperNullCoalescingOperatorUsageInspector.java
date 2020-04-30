package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ProperNullCoalescingOperatorUsageInspector extends BasePhpInspection {
    // Inspection options.
    public boolean ANALYZE_TYPES           = true;
    public boolean ALLOW_OVERLAPPING_TYPES = true;

    private static final String messageSimplify = "It possible to use '%s' instead (reduces cognitive load).";
    private static final String messageMismatch = "Resolved operands types are not complimentary, while they should be (%s vs %s).";

    @NotNull
    @Override
    public String getShortName() {
        return "ProperNullCoalescingOperatorUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Proper null-coalescing operator usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression binary) {
                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE && ! this.isPartOfCoalesce(binary) && ! this.isTypeCasted(binary)) {
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    if (left != null && right != null) {
                        /* case: `call() ?? null` */
                        if (PhpLanguageUtil.isNull(right)) {
                            if (left instanceof FunctionReference) {
                                holder.registerProblem(
                                        binary,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageSimplify), left.getText()),
                                        new UseLeftOperandFix(left.getText())
                                );
                            }
                            return;
                        }
                        /* case: `returns_string_or_null() ?? []` */
                        if (ANALYZE_TYPES && left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                            final Function scope = ExpressionSemanticUtil.getScope(binary);
                            if (scope != null) {
                                final Set<String> leftTypes = this.resolve((PhpTypedElement) left);
                                if (leftTypes != null && !leftTypes.isEmpty()) {
                                    final Set<String> rightTypes = this.resolve((PhpTypedElement) right);
                                    if (rightTypes != null && !rightTypes.isEmpty()) {
                                        final boolean complimentary = ALLOW_OVERLAPPING_TYPES
                                                ? rightTypes.stream().anyMatch(leftTypes::contains)
                                                : rightTypes.containsAll(leftTypes);
                                        if (! complimentary && ! this.areRelated(rightTypes, leftTypes)) {
                                            holder.registerProblem(
                                                    binary,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(messageMismatch), leftTypes.toString(), rightTypes.toString())
                                            );
                                        }
                                        rightTypes.clear();
                                    }
                                    leftTypes.clear();
                                }
                            }
                        }
                    }
                }
            }

            private boolean isTypeCasted(@NotNull BinaryExpression binary) {
                final PsiElement parent = binary.getParent();
                if (parent instanceof ParenthesizedExpression) {
                    final PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof UnaryExpression) {
                        final PsiElement operator = ((UnaryExpression) grandParent).getOperation();
                        return operator != null && PhpTokenTypes.tsCAST_OPS.contains(operator.getNode().getElementType());
                    }
                }
                return false;
            }

            private boolean isPartOfCoalesce(@NotNull BinaryExpression binary) {
                final PsiElement parent = binary.getParent();
                return parent instanceof BinaryExpression && ((BinaryExpression) parent).getOperationType() == PhpTokenTypes.opCOALESCE;
            }

            private boolean areRelated(@NotNull Set<String> rightTypes, @NotNull Set<String> leftTypes) {
                final Set<PhpClass> left = this.extractClasses(leftTypes);
                if (!left.isEmpty()) {
                    final Set<PhpClass> right = this.extractClasses(rightTypes);
                    if (!right.isEmpty() && left.stream().anyMatch(right::contains)) {
                        left.clear();
                        right.clear();
                        return true;
                    }
                    left.clear();
                }
                return false;
            }

            private HashSet<PhpClass> extractClasses(@NotNull Set<String> types) {
                final HashSet<PhpClass> classes = new HashSet<>();
                final PhpIndex index            = PhpIndex.getInstance(holder.getProject());
                types.stream().filter(t -> t.startsWith("\\")).forEach(t ->
                        OpenapiResolveUtil.resolveClassesAndInterfacesByFQN(t, index)
                                .forEach(c -> classes.addAll(InterfacesExtractUtil.getCrawlInheritanceTree(c, true)))
                );
                return classes;
            }

            @Nullable
            private Set<String> resolve(@NotNull PhpTypedElement subject) {
                final PhpType type = OpenapiResolveUtil.resolveType(subject, holder.getProject());
                if (type != null && !type.hasUnknown()) {
                    final Set<String> types = type.getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                    if (!types.isEmpty() && !types.contains(Types.strMixed) && !types.contains(Types.strObject)) {
                        types.remove(Types.strStatic);
                        types.remove(Types.strNull);
                        return types;
                    }
                    types.clear();
                }
                return null;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Verify complimentary operands types", ANALYZE_TYPES, (isSelected) -> ANALYZE_TYPES = isSelected);
            component.addCheckbox("Consider overlapping types complimentary", ALLOW_OVERLAPPING_TYPES, (isSelected) -> ALLOW_OVERLAPPING_TYPES = isSelected);
        });
    }

    private static final class UseLeftOperandFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use right operand instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseLeftOperandFix(@NotNull String expression) {
            super(expression);
        }
    }
}
