package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromArrayKeyExistsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromIssetStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromNullComparisonStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    // Inspection options.
    public boolean SUGGEST_SIMPLIFYING_TERNARIES = true;
    public boolean SUGGEST_SIMPLIFYING_IFS       = true;

    private static final String messagePattern = "'%s' can be used instead (reduces cognitive load).";

    private static final List<Function<TernaryExpression, String>> ternaryStrategies = new ArrayList<>();
    static {
        ternaryStrategies.add(GenerateAlternativeFromIssetStrategy::generate);
        ternaryStrategies.add(GenerateAlternativeFromNullComparisonStrategy::generate);
        ternaryStrategies.add(GenerateAlternativeFromArrayKeyExistsStrategy::generate);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "NullCoalescingOperatorCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Null coalescing operator can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (SUGGEST_SIMPLIFYING_TERNARIES && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP700)) {
                    for (final Function<TernaryExpression, String> strategy : ternaryStrategies) {
                        final String replacement = strategy.apply(expression);
                        if (replacement != null) {
                            holder.registerProblem(
                                    expression,
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    new ReplaceSingleConstructFix(replacement)
                            );
                            break;
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
                if (SUGGEST_SIMPLIFYING_IFS && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP700)) {
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
                        final String replacement = this.generateReplacement(
                                argument,
                                (AssignmentExpression) own,
                                (AssignmentExpression) previous
                        );
                        if (replacement != null) {
                            holder.registerProblem(
                                    expression.getFirstChild(),
                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                    new ReplaceMultipleConstructsFix(holder.getProject(), previous.getParent(), expression, replacement)
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
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                new ReplaceMultipleConstructsFix(holder.getProject(), expression, next, replacement)
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
                                            String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                            new ReplaceMultipleConstructsFix(holder.getProject(), expression, expression, replacement)
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
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                new ReplaceMultipleConstructsFix(holder.getProject(), expression, expression, replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean wrap(@NotNull PsiElement expression) {
                if (expression instanceof TernaryExpression || expression instanceof AssignmentExpression) {
                    return true;
                } else if (expression instanceof BinaryExpression) {
                    return ((BinaryExpression) expression).getOperationType() != PhpTokenTypes.opCOALESCE;
                }
                return false;
            }

            @Nullable
            private String generateReplacement(
                    @NotNull PsiElement argument,
                    @NotNull PhpReturn positive,
                    @NotNull PhpReturn negative
            ) {
                final PsiElement negativeValue = ExpressionSemanticUtil.getReturnValue(negative);
                if (negativeValue != null) {
                    final PsiElement positiveValue = ExpressionSemanticUtil.getReturnValue(positive);
                    if (positiveValue != null && OpenapiEquivalenceUtil.areEqual(argument, positiveValue)) {
                        final boolean isAnyAssignment = OpenapiTypesUtil.isAssignment(positiveValue) ||
                                                        OpenapiTypesUtil.isAssignment(negativeValue);
                        if (!isAnyAssignment) {
                            return String.format(
                                    "return %s ?? %s",
                                    String.format(this.wrap(positiveValue) ? "(%s)" : "%s", positiveValue.getText()),
                                    String.format(this.wrap(negativeValue) ? "(%s)" : "%s", negativeValue.getText())
                            );
                        }
                    }
                }
                return null;
            }

            @Nullable
            private String generateReplacement(
                    @NotNull PsiElement argument,
                    @NotNull AssignmentExpression positive,
                    @NotNull AssignmentExpression negative
            ) {
                final PsiElement negativeContainer = negative.getVariable();
                final PsiElement negativeValue     = negative.getValue();
                if (negativeContainer != null && negativeValue != null) {
                    final PsiElement positiveContainer = positive.getVariable();
                    final PsiElement positiveValue     = positive.getValue();
                    if (positiveContainer != null && positiveValue != null) {
                        final boolean matching = OpenapiEquivalenceUtil.areEqual(positiveContainer, negativeContainer) &&
                                                 OpenapiEquivalenceUtil.areEqual(argument, positiveValue);
                        if (matching) {
                            /* false-positives: array push */
                            final boolean isPush = PsiTreeUtil.findChildrenOfType(positiveContainer, ArrayIndex.class).stream()
                                    .anyMatch(index -> index.getValue() == null);
                            if (! isPush) {
                                /* false-positives: assignment by value */
                                final boolean isAnyByReference = OpenapiTypesUtil.isAssignmentByReference(positive) ||
                                                                 OpenapiTypesUtil.isAssignmentByReference(negative);
                                if (! isAnyByReference) {
                                    /* false-positives: assignment of processed container value */
                                    final boolean isContainerProcessing =
                                            PsiTreeUtil.findChildrenOfType(positiveValue, positiveContainer.getClass()).stream().anyMatch(c -> OpenapiEquivalenceUtil.areEqual(c, positiveContainer)) ||
                                            PsiTreeUtil.findChildrenOfType(negativeValue, negativeContainer.getClass()).stream().anyMatch(c -> OpenapiEquivalenceUtil.areEqual(c, negativeContainer));
                                    if (! isContainerProcessing) {
                                        PsiElement extractedNegative = negativeValue;
                                        while (extractedNegative != null && OpenapiTypesUtil.isAssignment(extractedNegative)) {
                                            extractedNegative = ((AssignmentExpression) extractedNegative).getValue();
                                        }
                                        if (extractedNegative != null) {
                                            return String.format(
                                                    "%s = %s ?? %s",
                                                    positiveContainer.getText(),
                                                    String.format(this.wrap(positiveValue) ? "(%s)" : "%s", positiveValue.getText()),
                                                    String.format(this.wrap(extractedNegative) ? "(%s)" : "%s", extractedNegative.getText())
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }

        };
    }

    private static final class ReplaceMultipleConstructsFix implements LocalQuickFix {
        private static final String title = "Replace with null coalescing operator";

        private final SmartPsiElementPointer<PsiElement> from;
        private final SmartPsiElementPointer<PsiElement> to;
        private final String replacement;

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        ReplaceMultipleConstructsFix(@NotNull Project project, @NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

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
                    if (from == to) {
                        final boolean wrap       = from instanceof If && from.getParent() instanceof Else;
                        final String replacement = wrap ? "{ " + this.replacement + "; }" : this.replacement + ";";
                        from.replace(PhpPsiElementFactory.createStatement(project, replacement));
                    } else {
                        final PsiElement scope = from.getParent();
                        /* inject the new construct */
                        scope.addBefore(PhpPsiElementFactory.createStatement(project, this.replacement + ";"), from);
                        /* deal with remaining multi-assignments if any */
                        if (OpenapiTypesUtil.isAssignment(from.getFirstChild())) {
                            final PsiElement value = ((AssignmentExpression) from.getFirstChild()).getValue();
                            if (OpenapiTypesUtil.isAssignment(value)) {
                                scope.addBefore(PhpPsiElementFactory.createStatement(project, value.getText() + ";"), from);
                            }
                        }
                        /* drop original constructs */
                        scope.deleteChildRange(from, to);
                    }
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Simplify ternary expressions", SUGGEST_SIMPLIFYING_TERNARIES, (isSelected) -> SUGGEST_SIMPLIFYING_TERNARIES = isSelected);
            component.addCheckbox("Simplify if-statements", SUGGEST_SIMPLIFYING_IFS, (isSelected) -> SUGGEST_SIMPLIFYING_IFS = isSelected);
        });
    }

    private static final class ReplaceSingleConstructFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use null coalescing operator instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        ReplaceSingleConstructFix(@NotNull String expression) {
            super(expression);
        }
    }
}