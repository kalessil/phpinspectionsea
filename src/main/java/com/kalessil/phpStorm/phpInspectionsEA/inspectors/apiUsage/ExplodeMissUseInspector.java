package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.codeInsight.PhpScopeHolder;
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlowUtil;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpAccessVariableInstruction;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExplodeMissUseInspector extends BasePhpInspection {
    private static final String message = "Consider refactoring with substr_count() instead (consumes less cpu and memory resources).";

    @NotNull
    public String getShortName() {
        return "ExplodeMissUseInspection";
    }

        @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general structure expectations */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (null == functionName || 1 != params.length || !functionName.equals("count")) {
                    return;
                }

                /* discover possible values */
                final HashSet<PsiElement> processed = new HashSet<>();
                final HashSet<PsiElement> values    = PossibleValuesDiscoveryUtil.discover(params[0], processed);
                processed.clear();

                /* do not analyze invariants */
                if (1 == values.size()) {
                    final PsiElement value = values.iterator().next();
                    values.clear();

                    if (OpenapiTypesUtil.isFunctionReference(value)) {
                        /* inner call must be explode() */
                        final String innerFunctionName = ((FunctionReference) value).getName();
                        if (null == innerFunctionName || !innerFunctionName.equals("explode")) {
                            return;
                        }

                        /* if the parameter is a variable, ensure it used only 2 times (write, read) */
                        if (params[0] instanceof Variable) {
                            final PhpScopeHolder parentScope = ExpressionSemanticUtil.getScope(reference);
                            if (null != parentScope) {
                                final PhpAccessVariableInstruction[] usages
                                        = PhpControlFlowUtil.getFollowingVariableAccessInstructions(
                                            parentScope.getControlFlow().getEntryPoint(),
                                            ((Variable) params[0]).getName(), false
                                          );
                                if (2 != usages.length) {
                                    return;
                                }
                            }
                        }

                        holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
                values.clear();
            }
        };
    }
}
