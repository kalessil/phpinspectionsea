package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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

public class ExplodeMissUseInspector extends BasePhpInspection {
    private static final String messagePattern = "Consider using '%e%' instead (consumes less cpu and memory resources).";

    private static final Map<String, String> semanticMapping = new HashMap<>();
    static {
        semanticMapping.put("count",   "substr_count(%s%, %f%) + 1");
        // semanticMapping.put("current", "strstr(%s%, %f%, true)"); if fragment missing, strstr changes behaviour
    }

    @NotNull
    public String getShortName() {
        return "ExplodeMissUseInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* general structure expectations */
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (null == functionName || 1 != arguments.length || !semanticMapping.containsKey(functionName)) {
                    return;
                }

                /* discover possible values */
                final Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(arguments[0]);

                /* do not analyze invariants */
                if (1 == values.size()) {
                    final PsiElement value = values.iterator().next();
                    values.clear();

                    if (OpenapiTypesUtil.isFunctionReference(value)) {
                        /* inner call must be explode() */
                        final FunctionReference innerCall = (FunctionReference) value;
                        final String innerFunctionName    = innerCall.getName();
                        final PsiElement[] innerParams    = innerCall.getParameters();
                        if (null == innerFunctionName || 2 != innerParams.length || !innerFunctionName.equals("explode")) {
                            return;
                        }

                        /* if the parameter is a variable, ensure it used only 2 times (write, read) */
                        if (arguments[0] instanceof Variable) {
                            final PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(reference);
                            if (null != parentScope) {
                                final PhpAccessVariableInstruction[] usages
                                    = PhpControlFlowUtil.getFollowingVariableAccessInstructions
                                      (
                                          parentScope.getControlFlow().getEntryPoint(),
                                          ((Variable) arguments[0]).getName(),
                                          false
                                      );
                                if (2 != usages.length) {
                                    return;
                                }
                            }
                        }

                        final String replacement = semanticMapping.get(functionName)
                                .replace("%f%", innerParams[0].getText())
                                .replace("%s%", innerParams[1].getText());
                        final String message = messagePattern.replace("%e%", replacement);
                        if (arguments[0] == value) {
                            holder.registerProblem(reference, message, new UseAlternativeFix(replacement));
                        } else {
                            holder.registerProblem(reference, message);
                        }
                    }
                }
                values.clear();
            }
        };
    }

    private class UseAlternativeFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Use the suggested alternative";
        }

        UseAlternativeFix(@NotNull String expression) {
            super(expression);
        }
    }
}
