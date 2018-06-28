package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class ArrayColumnCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' would fit more here (it also faster, but loses original keys).";

    @NotNull
    public String getShortName() {
        return "ArrayColumnCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (!EAUltimateApplicationComponent.areFeaturesEnabled() || php.compareTo(PhpLanguageLevel.PHP550) < 0) {
                    return;
                }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("array_map")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 2 && arguments[1] != null && OpenapiTypesUtil.isLambda(arguments[0])) {
                        final Function closure       = (Function) (arguments[0] instanceof Function ? arguments[0] : arguments[0].getFirstChild());
                        final Parameter[] parameters = closure.getParameters();
                        if (parameters.length > 0) {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(closure);
                            if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) == 1) {
                                final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                                if (last instanceof PhpReturn) {
                                    final PsiElement candidate = ExpressionSemanticUtil.getReturnValue((PhpReturn) last);
                                    if (candidate instanceof ArrayAccessExpression) {
                                        final ArrayAccessExpression access = (ArrayAccessExpression) candidate;
                                        final PhpPsiElement value          = access.getValue();
                                        if (value instanceof Variable && parameters[0].getName().equals(value.getName())) {
                                            final ArrayIndex index = access.getIndex();
                                            final PsiElement key   = index == null ? null : index.getValue();
                                            if (key != null) {
                                                    final String replacement = String.format(
                                                            "array_column(%s, %s)",
                                                            arguments[1].getText(),
                                                            key.getText()
                                                    );
                                                    holder.registerProblem(
                                                            reference,
                                                            String.format(messagePattern, replacement),
                                                            new UseArrayColumnFixer(replacement)
                                                    );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseArrayColumnFixer extends UseSuggestedReplacementFixer {
        private static final String title = "Use array_column(...) instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseArrayColumnFixer(@NotNull String expression) {
            super(expression);
        }
    }
}
