package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NullableArgumentComparisonStrategy {
    private static final String messagePattern = "This might work not as expected (an argument can be null/false), use '%s' to be sure.";

    private static final Map<IElementType, String> mapping = new HashMap<>();
    static {
        mapping.put(PhpTokenTypes.opLESS,          ">=");
        mapping.put(PhpTokenTypes.opLESS_OR_EQUAL, ">");
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        boolean result              = false;
        final IElementType operator = expression.getOperationType();
        if (mapping.containsKey(operator)) {
            PsiElement parent = expression.getParent();
            while (parent instanceof ParenthesizedExpression) {
                parent = parent.getParent();
            }
            final PsiElement argument = expression.getLeftOperand();
            final PsiElement value    = expression.getRightOperand();
            if (parent instanceof UnaryExpression && argument instanceof PhpTypedElement && value != null) {
                final UnaryExpression target = (UnaryExpression) parent;
                if (OpenapiTypesUtil.is(target.getOperation(), PhpTokenTypes.opNOT)) {
                    final PhpType type = OpenapiResolveUtil.resolveType((PhpTypedElement) argument, expression.getProject());
                    if (type != null && !type.hasUnknown()) {
                        final Set<String> types = new HashSet<>();
                        type.getTypes().forEach(t -> types.add(Types.getType(t)));
                        if (types.contains(Types.strNull) || types.contains(Types.strBoolean)) {
                            final String replacement
                                    = String.format("%s %s %s", argument.getText(), mapping.get(operator), value.getText());
                            final String message = String.format(messagePattern, replacement);
                            holder.registerProblem(target, message, new NullableArgumentComparisonFix(replacement));
                            result = true;
                        }
                        types.clear();
                    }
                }
            }
        }
        return result;
    }

    private static final class NullableArgumentComparisonFix extends UseSuggestedReplacementFixer {
        private static final String title = "Secure the comparison";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        NullableArgumentComparisonFix(@NotNull String expression) {
            super(expression);
        }
    }
}
