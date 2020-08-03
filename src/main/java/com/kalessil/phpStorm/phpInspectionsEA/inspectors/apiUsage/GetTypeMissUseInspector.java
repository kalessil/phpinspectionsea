package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class GetTypeMissUseInspector extends BasePhpInspection {
    private static final String messageUseFunctionPattern = "'%s' would fit more here (clearer expresses the intention and SCA friendly).";
    private static final String messageInvalidPattern     = "'%s' is not a value returned by 'gettype(...)'.";

    @NotNull
    @Override
    public String getShortName() {
        return "GetTypeMissUseInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'gettype(...)' could be replaced with 'is_*(...)'";
    }

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("boolean",  "is_bool");
        mapping.put("integer",  "is_int");
        mapping.put("double",   "is_float");
        mapping.put("string",   "is_string");
        mapping.put("array",    "is_array");
        mapping.put("object",   "is_object");
        mapping.put("resource", "is_resource");
        mapping.put("NULL",     "is_null");
        // "unknown type" will not be processed: there is no is_* analog in API
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("gettype")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        final PsiElement parent = reference.getParent();
                        if (parent instanceof BinaryExpression) {
                            final BinaryExpression expression = (BinaryExpression) parent;
                            final IElementType operator       = expression.getOperationType();
                            if (OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                                final PsiElement candidate          = OpenapiElementsUtil.getSecondOperand(expression, reference);
                                final StringLiteralExpression value = ExpressionSemanticUtil.resolveAsStringLiteral(candidate);
                                if (value != null) {
                                    final String type = value.getContents();
                                    if (!mapping.containsKey(type)) {
                                        /* edge case: compared string is wrong xD - bug */
                                        if (!type.equals("unknown type") && !type.equals("resource (closed)")) {
                                            holder.registerProblem(
                                                    value,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(messageInvalidPattern), type),
                                                    ProblemHighlightType.GENERIC_ERROR
                                            );
                                        }
                                    } else {
                                        final boolean isInverted = operator == PhpTokenTypes.opNOT_EQUAL || operator == PhpTokenTypes.opNOT_IDENTICAL;
                                        final String replacement = String.format("%s%s(%s)", isInverted ? "!" : "", mapping.get(type), arguments[0].getText());
                                        holder.registerProblem(
                                                parent,
                                                String.format(MessagesPresentationUtil.prefixWithEa(messageUseFunctionPattern), replacement),
                                                new UseSuggestedFunctionFix(replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseSuggestedFunctionFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use suggested is_*(...) function instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseSuggestedFunctionFix(@NotNull String expression) {
            super(expression);
        }
    }
}
