package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromArrayKeyExistsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromIssetStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromNullComparisonStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class NullCoalescingOperatorCanBeUsedInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' construction should be used instead.";

    private static final List<Function<TernaryExpression, String>> ternaryStrategies = new ArrayList<>();
    static {
        ternaryStrategies.add(GenerateAlternativeFromIssetStrategy::generate);
        ternaryStrategies.add(GenerateAlternativeFromNullComparisonStrategy::generate);
        ternaryStrategies.add(GenerateAlternativeFromArrayKeyExistsStrategy::generate);
    }

    @NotNull
    public String getShortName() {
        return "NullCoalescingOperatorCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    for (final Function<TernaryExpression, String> strategy : ternaryStrategies) {
                        final String replacement = strategy.apply(expression);
                        if (replacement != null) {
                            holder.registerProblem(
                                    expression,
                                    String.format(messagePattern, replacement),
                                    new UseTheOperatorFix(replacement)
                            );
                            break;
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    final PsiElement condition = expression.getCondition();
                    if (condition instanceof PhpIsset) {
                        final PhpIsset isset         = (PhpIsset) condition;
                        final PsiElement[] arguments = isset.getVariables();
                        if (arguments.length == 1 && expression.getElseIfBranches().length == 0) {
                            final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(expression);
                            if (ifBody != null && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                                final Else alternative = expression.getElseBranch();
                                if (alternative == null) {
                                    PsiElement previous = expression.getPrevPsiSibling();
                                    PsiElement own      = ExpressionSemanticUtil.getLastStatement(ifBody);
                                    if (previous != null && own != null) {
                                        previous = previous.getFirstChild();
                                        own      = own.getFirstChild();
                                        if (OpenapiTypesUtil.isAssignment(previous) && OpenapiTypesUtil.isAssignment(own)) {
                                            final AssignmentExpression negative = (AssignmentExpression) previous;
                                            final AssignmentExpression positive = (AssignmentExpression) own;
                                            // same container, value is isset argument
                                        }
                                    }
                                } else {
                                    final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(alternative);
                                    if (elseBody != null && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                                        PsiElement ownFromIf   = ExpressionSemanticUtil.getLastStatement(ifBody);
                                        PsiElement ownFromElse = ExpressionSemanticUtil.getLastStatement(elseBody);

                                        // if body is returning isset argument else body returning whatever
                                        // if body is assigning isset argument else body assigning whatever (same container)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseTheOperatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null coalescing operator instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        UseTheOperatorFix(@NotNull String expression) {
            super(expression);
        }
    }
}