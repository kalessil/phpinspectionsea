package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
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

public class DynamicCallsToScopeIntrospectionInspector extends BasePhpInspection {
    private static final String messagePattern = "Emits a runtime warning (cannot call %s() dynamically).";

    final private static Map<String, Integer> targetCalls        = new HashMap<>();
    final private static Map<String, Integer> callbacksPositions = new HashMap<>();
    static {
        targetCalls.put("compact",         -1);
        targetCalls.put("extract",         -1);
        targetCalls.put("func_get_args",    0);
        targetCalls.put("func_get_arg",     1);
        targetCalls.put("func_num_args",    0);
        targetCalls.put("get_defined_vars", 0);
        targetCalls.put("mb_parse_str",     1);
        targetCalls.put("parse_str",        1);
        callbacksPositions.put("call_user_func",       0);
        callbacksPositions.put("call_user_func_array", 0);
        callbacksPositions.put("array_filter",         1);
        callbacksPositions.put("array_map",            0);
        callbacksPositions.put("array_reduce",         1);
        callbacksPositions.put("array_walk",           1);
        callbacksPositions.put("array_walk_recursive", 1);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "DynamicCallsToScopeIntrospectionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Deprecated dynamic calls to scope introspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP710)) {
                    final String functionName = reference.getName();
                    /* discover target element */
                    final PsiElement target;
                    if (StringUtils.isEmpty(functionName)) {
                        final PsiElement[] children = reference.getChildren();
                        target = children.length == 2 ? children[0] : null;
                    } else if (callbacksPositions.containsKey(functionName)) {
                        final int callbackPosition   = callbacksPositions.get(functionName);
                        final PsiElement[] arguments = reference.getParameters();
                        target = arguments.length >= callbackPosition + 1 ? arguments[callbackPosition] : null;
                    } else {
                        target = null;
                    }
                    /* discover the target function */
                    if (target != null) {
                        final StringLiteralExpression literal = ExpressionSemanticUtil.resolveAsStringLiteral(target);
                        if (literal != null) {
                            final String raw      = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                            final String callback = raw.startsWith("\\") ? raw.substring(1) : raw;
                            if (targetCalls.containsKey(callback)) {
                                holder.registerProblem(
                                        target,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), callback)
                                );
                            }
                        }
                    }
                }
            }
        };
    }
}
