package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ExplodeLimitUsageInspector extends PhpInspection {
    private static final String messagePattern = "'%s' would fit more here (consumes less cpu and memory resources).";

    @NotNull
    @Override
    public String getShortName() {
        return "ExplodeLimitUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "'explode(...)' limit can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("explode")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && this.isTargetContext(reference)) {
                        final String replacement = String.format(
                                "%sexplode(%s, %s, 2)",
                                reference.getImmediateNamespaceName(),
                                arguments[0].getText(),
                                arguments[1].getText()
                        );
                        holder.registerProblem(
                                reference,
                                String.format(messagePattern, replacement),
                                new AddLimitArgumentFixer(replacement)
                        );
                    }
                }
            }

            private boolean isTargetContext(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof ArrayAccessExpression) {
                    final ArrayIndex index = ((ArrayAccessExpression) parent).getIndex();
                    if (index != null) {
                        final PsiElement indexValue = index.getValue();
                        return indexValue != null && OpenapiTypesUtil.isNumber(indexValue) && indexValue.getText().equals("0");
                    }
                } else if (OpenapiTypesUtil.isAssignment(parent)) {
                    final PsiElement container = ((AssignmentExpression) parent).getVariable();
                    if (container instanceof Variable) {
                        final Function scope = ExpressionSemanticUtil.getScope(expression);
                        if (scope != null) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(scope);
                            if (body != null) {
                                final Variable variable      = (Variable) container;
                                final String variableName    = variable.getName();
                                boolean reachedStartingPoint = false;
                                boolean result               = false;
                                for (final Variable match : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                    reachedStartingPoint = reachedStartingPoint || match == variable;
                                    if (reachedStartingPoint && match != variable && variableName.equals(match.getName())) {
                                        final PsiElement context = match.getParent();
                                        if (!(result = context instanceof ArrayAccessExpression && this.isTargetContext(match))) {
                                            break;
                                        }
                                    }
                                }
                                return result;
                            }
                        }
                    }
                }

                return false;
            }
        };
    }

    private static final class AddLimitArgumentFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Add limit to the 'explode(...)' call";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        AddLimitArgumentFixer(@NotNull String expression) {
            super(expression);
        }
    }
}
