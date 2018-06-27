package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

public class UnnecessaryClosureInspector extends BasePhpInspection {
    private static final String messagePattern = "The closure can be replaced with '%s' (reduces cognitive load).";

    final private static Map<String, Integer> closurePositions = new HashMap<>();
    static {
        closurePositions.put("array_filter", 1);
        closurePositions.put("array_map", 0);
        closurePositions.put("array_walk", 1);
        closurePositions.put("array_reduce", 1);
    }

    @NotNull
    public String getShortName() {
        return "UnnecessaryClosureInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && closurePositions.containsKey(functionName)) {
                    final int targetPosition     = closurePositions.get(functionName);
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > targetPosition && OpenapiTypesUtil.isLambda(arguments[targetPosition])) {
                        final PsiElement expression = arguments[targetPosition];
                        final Function closure      = (Function) (expression instanceof Function ? expression : expression.getFirstChild());
                        if (closure.getParameters().length > 0) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(closure);
                            if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                                final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                                if (last instanceof PhpReturn) {
                                    final PsiElement candidate = ExpressionSemanticUtil.getReturnValue((PhpReturn) last);
                                    if (OpenapiTypesUtil.isFunctionReference(candidate)) {
                                        final FunctionReference callback = (FunctionReference) candidate;
                                        final boolean isTarget           = this.canInline(callback, closure);
                                        if (isTarget) {
                                            final String callbackName = callback.getName();
                                            holder.registerProblem(
                                                    closure,
                                                    String.format(messagePattern, callbackName),
                                                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                    new UseCallbackFix(String.format("'%s'", callbackName))
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean canInline(@NotNull FunctionReference callback, @NotNull Function closure) {
                boolean result               = false;
                final PsiElement[] arguments = callback.getParameters();
                if (arguments.length > 0) {
                    final Parameter[] input = closure.getParameters();
                    if (input.length == arguments.length && Arrays.stream(arguments).allMatch(a -> a instanceof Variable)) {
                        result = true;
                        for (int index = 0; index < arguments.length; ++index) {
                            final Variable argument       = (Variable) arguments[index];
                            final boolean isEnforcingType = !input[index].getDeclaredType().isEmpty();
                            if (isEnforcingType || !argument.getName().equals(input[index].getName())) {
                                result = false;
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        };
    }

    private static final class UseCallbackFix extends UseSuggestedReplacementFixer {
        private static final String title = "Inline the callable";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseCallbackFix(@NotNull String expression) {
            super(expression);
        }
    }
}
