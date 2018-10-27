package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
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
import org.jetbrains.annotations.Nullable;

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
    private static final String messagePattern = "'%s' can be used instead (reduces cognitive load).";

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
                if (this.isContainingFileSkipped(expression)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    for (final Function<TernaryExpression, String> strategy : ternaryStrategies) {
                        final String replacement = strategy.apply(expression);
                        if (replacement != null) {
                            holder.registerProblem(
                                    expression,
                                    String.format(messagePattern, replacement),
                                    new ReplaceSingleConstructFix(replacement)
                            );
                            break;
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
                if (this.isContainingFileSkipped(expression)) { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    final PsiElement condition = expression.getCondition();
                    if (condition instanceof PhpIsset) {
                        final PhpIsset isset         = (PhpIsset) condition;
                        final PsiElement[] arguments = isset.getVariables();
                        if (arguments.length == 1 && expression.getElseIfBranches().length == 0) {
                            final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(expression);
                            if (ifBody != null && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                                if (expression.getElseBranch() == null) {
                                    this.analyzeIfWithPrecedingStatement(expression, arguments[0], ifBody);
                                    this.analyzeIfWithFollowingStatement(expression, arguments[0], ifBody);
                                } else {
                                    this.analyzeIfElseStatement(expression, arguments[0], ifBody);
                                }
                            }
                        }
                    }
                }
            }

            private void analyzeIfWithPrecedingStatement(
                    @NotNull If expression,
                    @NotNull PsiElement argument,
                    @NotNull GroupStatement ifBody
            ) {
                PsiElement previous = expression.getPrevPsiSibling();
                PsiElement own      = ExpressionSemanticUtil.getLastStatement(ifBody);
                if (previous != null && own != null) {
                    previous = previous.getFirstChild();
                    own      = own.getFirstChild();
                    if (OpenapiTypesUtil.isAssignment(previous) && OpenapiTypesUtil.isAssignment(own)) {
                        final String replacement = this.generateReplacement(argument, (AssignmentExpression) own, (AssignmentExpression) previous);
                        if (replacement != null) {
                            holder.registerProblem(
                                    expression.getFirstChild(),
                                    String.format(messagePattern, replacement),
                                    new ReplaceMultipleConstructsFix(previous.getParent(), expression, replacement)
                            );
                        }
                    }
                }
            }

            private void analyzeIfWithFollowingStatement(
                    @NotNull If expression,
                    @NotNull PsiElement argument,
                    @NotNull GroupStatement ifBody
            ) {
                final PsiElement next = expression.getNextPsiSibling();
                final PsiElement own  = ExpressionSemanticUtil.getLastStatement(ifBody);
                if (next instanceof PhpReturn && own instanceof PhpReturn) {
                    final String replacement = this.generateReplacement(argument, (PhpReturn) own, (PhpReturn) next);
                    if (replacement != null) {
                        holder.registerProblem(
                                expression.getFirstChild(),
                                String.format(messagePattern, replacement),
                                new ReplaceMultipleConstructsFix(expression, next, replacement)
                        );
                    }
                }
            }

            private void analyzeIfElseStatement(
                    @NotNull If expression,
                    @NotNull PsiElement argument,
                    @NotNull GroupStatement ifBody
            ) {
                final Else alternative = expression.getElseBranch();
                if (alternative != null) {
                    final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(alternative);
                    if (elseBody != null && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                        PsiElement ownFromIf   = ExpressionSemanticUtil.getLastStatement(ifBody);
                        PsiElement ownFromElse = ExpressionSemanticUtil.getLastStatement(elseBody);
                        if (ownFromIf != null && ownFromElse != null) {
                            if (ownFromIf instanceof PhpReturn && ownFromElse instanceof PhpReturn) {
                                final String replacement = this.generateReplacement(argument, (PhpReturn) ownFromIf, (PhpReturn) ownFromElse);
                                if (replacement != null) {
                                    holder.registerProblem(
                                            expression.getFirstChild(),
                                            String.format(messagePattern, replacement),
                                            new ReplaceMultipleConstructsFix(expression, expression, replacement)
                                    );
                                }
                            } else {
                                ownFromIf   = ownFromIf.getFirstChild();
                                ownFromElse = ownFromElse.getFirstChild();
                                if (OpenapiTypesUtil.isAssignment(ownFromIf) && OpenapiTypesUtil.isAssignment(ownFromElse)) {
                                    final String replacement = this.generateReplacement(argument, (AssignmentExpression) ownFromIf, (AssignmentExpression) ownFromElse);
                                    if (replacement != null) {
                                        holder.registerProblem(
                                                expression.getFirstChild(),
                                                String.format(messagePattern, replacement),
                                                new ReplaceMultipleConstructsFix(expression, expression, replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private String generateReplacement(
                    @NotNull PsiElement argument,
                    @NotNull PhpReturn positive,
                    @NotNull PhpReturn negative
            ) {
                String result                  = null;
                final PsiElement negativeValue = ExpressionSemanticUtil.getReturnValue(negative);
                if (negativeValue != null) {
                    final PsiElement positiveValue = ExpressionSemanticUtil.getReturnValue(positive);
                    if (positiveValue != null && OpenapiEquivalenceUtil.areEqual(argument, positiveValue)) {
                            result = String.format("return %s ?? %s", positiveValue.getText(), negativeValue.getText());
                    }
                }
                return result;
            }

            @Nullable
            private String generateReplacement(
                    @NotNull PsiElement argument,
                    @NotNull AssignmentExpression positive,
                    @NotNull AssignmentExpression negative
            ) {
                String result                      = null;
                final PsiElement negativeContainer = negative.getVariable();
                final PsiElement negativeValue     = negative.getValue();
                if (negativeContainer != null && negativeValue != null) {
                    final PsiElement positiveContainer = positive.getVariable();
                    final PsiElement positiveValue     = positive.getValue();
                    if (positiveContainer != null && positiveValue != null) {
                        final boolean check = OpenapiEquivalenceUtil.areEqual(positiveContainer, negativeContainer);
                        if (check && OpenapiEquivalenceUtil.areEqual(argument, positiveValue)) {
                            result = String.format(
                                    "%s = %s ?? %s",
                                    positiveContainer.getText(),
                                    positiveValue.getText(),
                                    negativeValue.getText()
                            );
                        }
                    }
                }
                return result;
            }
        };
    }

    private static final class ReplaceMultipleConstructsFix implements LocalQuickFix {
        private static final String title = "Use null coalescing operator instead";

        private final SmartPsiElementPointer<PsiElement> from;
        private final SmartPsiElementPointer<PsiElement> to;
        private final String replacement;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        ReplaceMultipleConstructsFix(@NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(from.getProject());

            this.from        = factory.createSmartPsiElementPointer(from);
            this.to          = factory.createSmartPsiElementPointer(to);
            this.replacement = replacement;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement from = this.from.getElement();
                final PsiElement to   = this.to.getElement();
                if (from != null && to != null) {
                    final PsiElement implant = PhpPsiElementFactory.createStatement(project, this.replacement + ";");
                    if (from == to) {
                        from.replace(implant);
                    } else {
                        from.getParent().addBefore(implant, from);
                        from.getParent().deleteChildRange(from, to);
                    }
                }
            }
        }
    }

    private static final class ReplaceSingleConstructFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null coalescing operator instead";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        ReplaceSingleConstructFix(@NotNull String expression) {
            super(expression);
        }
    }
}