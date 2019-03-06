package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    public boolean ANALYZE_TYPES = true;

    private static final String messageSimplify = "It possible to use '%s' instead (reduces cognitive load).";
    private static final String messageMismatch = "Resolved operands types are not complimentary, while they should be (%s vs %s).";

    @NotNull
    public String getShortName() {
        return "ProperNullCoalescingOperatorUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression binary) {
                if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                    final PsiElement left  = binary.getLeftOperand();
                    final PsiElement right = binary.getRightOperand();
                    if (left != null && right != null) {
                        /* case: `call() ?? null` */
                        if (left instanceof FunctionReference && PhpLanguageUtil.isNull(right)) {
                            final String replacement = left.getText();
                            holder.registerProblem(
                                    binary,
                                    String.format(messageSimplify, replacement),
                                    new UseLeftOperandFix(replacement)
                            );
                        }
                        /* case: `returns_string_or_null() ?? []` */
                        if (ANALYZE_TYPES && left instanceof PhpTypedElement && right instanceof PhpTypedElement) {
                            final Function scope = ExpressionSemanticUtil.getScope(binary);
                            if (scope != null) {
                                final Set<String> leftTypes = this.resolve((PhpTypedElement) left);
                                if (leftTypes != null) {
                                    final Set<String> rightTypes = this.resolve((PhpTypedElement) right);
                                    if (rightTypes != null && !leftTypes.containsAll(rightTypes)) {
                                        holder.registerProblem(
                                                binary,
                                                String.format(messageMismatch, leftTypes.toString(), rightTypes.toString())
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private Set<String> resolve(@NotNull PhpTypedElement subject) {
                final PhpType type = OpenapiResolveUtil.resolveType(subject, holder.getProject());
                if (type != null && !type.hasUnknown()) {
                    final Set<String> types = type.getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                    if (!types.isEmpty() && !types.contains(Types.strMixed) && !types.contains(Types.strObject)) {
                        return types;
                    }
                    types.clear();
                }
                return null;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Verify complimentary operand types", ANALYZE_TYPES, (isSelected) -> ANALYZE_TYPES = isSelected)
        );
    }

    private static final class UseLeftOperandFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use right operand instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseLeftOperandFix(@NotNull String expression) {
            super(expression);
        }
    }
}
