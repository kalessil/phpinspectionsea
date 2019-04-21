package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromArrayKeyExistsStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromIssetStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy.GenerateAlternativeFromNullComparisonStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
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
    public String getShortName() {
        return "NullCoalescingOperatorCanBeUsedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(expression))             { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (SUGGEST_SIMPLIFYING_TERNARIES && php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
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
            public void visitPhpIf(@NotNull If statement) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(statement))              { return; }

                final PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (SUGGEST_SIMPLIFYING_IFS && php.hasFeature(PhpLanguageFeature.COALESCE_OPERATOR)) {
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                    if (condition != null && statement.getElseIfBranches().length == 0) {
                        final PsiElement extracted = this.getTargetCondition(condition);
                        if (extracted != null) {
                            final Couple<Couple<PsiElement>> fragments = this.extract(statement);
                            final PsiElement firstValue                = fragments.second.first;
                            final PsiElement secondValue               = fragments.second.second;
                            if (firstValue != null && secondValue != null) {
                                /* generate replacement */
                                String coalescing = null;
                                if (extracted instanceof PhpIsset) {
                                    coalescing = this.generateReplacementForIsset(condition, (PhpIsset) extracted, firstValue, secondValue);
                                } else if (extracted instanceof FunctionReference) {
                                    //replacement = this.generateReplacementForExists(condition, extracted, firstValue, secondValue);
                                } else if (extracted instanceof BinaryExpression) {
                                    //replacement = this.generateReplacementForIdentity(condition, extracted, firstValue, secondValue);
                                }
                                /* emit violation if can offer replacement */
                                if (coalescing != null) {
                                    final PsiElement context = firstValue.getParent();
                                    if (context instanceof PhpReturn) {
                                        final String replacement = String.format("return %s", coalescing);
                                        holder.registerProblem(
                                                statement.getFirstChild(),
                                                String.format(messagePattern, replacement),
                                                new ReplaceMultipleConstructFix(fragments.first.first, fragments.first.second, replacement)
                                        );
                                    } else if (context instanceof AssignmentExpression) {
                                        final PsiElement container = ((AssignmentExpression) context).getVariable();
                                        final String replacement   = String.format("%s = %s", container.getText(), coalescing);
                                        holder.registerProblem(
                                                statement.getFirstChild(),
                                                String.format(messagePattern, replacement),
                                                new ReplaceMultipleConstructFix(fragments.first.first, fragments.first.second, replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Nullable
            private String generateReplacementForIsset(
                    @NotNull PsiElement condition,
                    @NotNull PhpIsset extracted,
                    @NotNull PsiElement first,
                    @NotNull PsiElement second
            ) {
                final PsiElement subject = extracted.getVariables()[0];
                if (subject != null) {
                    final boolean expectsToBeSet = condition == extracted;
                    final PsiElement candidate   = expectsToBeSet ? first : second;
                    if (OpenapiEquivalenceUtil.areEqual(candidate, subject)) {
                        final PsiElement alternative = expectsToBeSet ? second : first;
                        return String.format("%s ?? %s", candidate.getText(), alternative.getText());
                    }
                }
                return null;
            }

            @Nullable
            private PsiElement getTargetCondition(@NotNull PsiElement condition) {
                /* un-wrap inverted conditions */
                if (condition instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) condition;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                        condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                    }
                }
                /* do check */
                if (condition instanceof PhpIsset) {
                    final PhpIsset isset = (PhpIsset) condition;
                    if (isset.getVariables().length == 1) {
                        return condition;
                    }
                } else if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    final IElementType operator   = binary.getOperationType();
                    if (operator == PhpTokenTypes.opIDENTICAL || operator == PhpTokenTypes.opNOT_IDENTICAL) {
                        if (PhpLanguageUtil.isNull(binary.getRightOperand())) {
                            return condition;
                        } else if (PhpLanguageUtil.isNull(binary.getLeftOperand())) {
                            return condition;
                        }
                    }
                } else if (OpenapiTypesUtil.isFunctionReference(condition)) {
                    final String functionName = ((FunctionReference) condition).getName();
                    if (functionName != null && functionName.equals("array_key_exists")) {
                        return condition;
                    }
                }
                return null;
            }

            /* first pair: what to drop, second positive and negative branching values */
            private Couple<Couple<PsiElement>> extract(@NotNull If statement) {
                Couple<Couple<PsiElement>> result = new Couple<>(new Couple<>(null, null), new Couple<>(null, null));

                final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(statement);
                if (ifBody != null && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                    final PsiElement ifLast = this.extractCandidate(ExpressionSemanticUtil.getLastStatement(ifBody));
                    if (ifLast != null) {
                        /* extract all related constructs */
                        final PsiElement ifNext     = this.extractCandidate(statement.getNextPsiSibling());
                        final PsiElement ifPrevious = this.extractCandidate(statement.getPrevPsiSibling());

                        if (statement.getElseBranch() != null) {
                            PsiElement elseLast           = null;
                            final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(statement.getElseBranch());
                            if (elseBody != null && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                                elseLast = this.extractCandidate(ExpressionSemanticUtil.getLastStatement(elseBody));
                            }

                            /* if - return - else - return */
                            if (ifLast instanceof PhpReturn && elseLast instanceof PhpReturn) {
                                result = new Couple<>(
                                        new Couple<>(statement, statement),
                                        new Couple<>(((PhpReturn) ifLast).getArgument(), ((PhpReturn) elseLast).getArgument())
                                );
                            }
                            /* if - assign - else - assign */
                            else if (ifLast instanceof AssignmentExpression && elseLast instanceof AssignmentExpression) {
                                final AssignmentExpression ifAssignment   = (AssignmentExpression) ifLast;
                                final AssignmentExpression elseAssignment = (AssignmentExpression) elseLast;
                                final PsiElement ifContainer              = ifAssignment.getVariable();
                                final PsiElement elseContainer            = elseAssignment.getVariable();
                                if (ifContainer instanceof Variable && elseContainer instanceof Variable) {
                                    final boolean isTarget = OpenapiEquivalenceUtil.areEqual(ifContainer, elseContainer);
                                    if (isTarget) {
                                        result = new Couple<>(
                                                new Couple<>(statement, statement),
                                                new Couple<>(ifAssignment.getValue(), elseAssignment.getValue())
                                        );
                                    }
                                }
                            }
                        } else {
                            /* assign - if - assign */
                            if (ifPrevious instanceof AssignmentExpression && ifLast instanceof AssignmentExpression) {
                                final AssignmentExpression previousAssignment = (AssignmentExpression) ifPrevious;
                                final AssignmentExpression ifAssignment       = (AssignmentExpression) ifLast;
                                final PsiElement previousContainer            = previousAssignment.getVariable();
                                final PsiElement ifContainer                  = ifAssignment.getVariable();
                                if (previousContainer instanceof Variable && ifContainer instanceof Variable) {
                                    final boolean isTarget = OpenapiEquivalenceUtil.areEqual(previousContainer, ifContainer);
                                    if (isTarget && !OpenapiTypesUtil.isAssignmentByReference(previousAssignment)) {
                                        final PsiElement previousValue = previousAssignment.getValue();
                                        if (!(previousValue instanceof AssignmentExpression)) {
                                            result = new Couple<>(
                                                    new Couple<>(ifPrevious.getParent(), statement),
                                                    new Couple<>(ifAssignment.getValue(), previousValue)
                                            );
                                        }
                                    }
                                }
                            }
                            /* if - return - return */
                            else if (ifLast instanceof PhpReturn && ifNext instanceof PhpReturn) {
                                result = new Couple<>(
                                        new Couple<>(statement, ifNext),
                                        new Couple<>(((PhpReturn) ifLast).getArgument(), ((PhpReturn) ifNext).getArgument())
                                );
                            }
                        }
                    }
                }
                return result;
            }

            @Nullable
            private PsiElement extractCandidate(@Nullable PsiElement statement) {
                if (statement instanceof PhpReturn) {
                    return statement;
                } else if (OpenapiTypesUtil.isStatementImpl(statement)) {
                    final PsiElement possiblyAssignment = statement.getFirstChild();
                    if (OpenapiTypesUtil.isAssignment(possiblyAssignment)) {
                        final AssignmentExpression assignment = (AssignmentExpression) possiblyAssignment;
                        final PsiElement container             = assignment.getVariable();
                        if (container instanceof Variable) {
                            return assignment;
                        }
                    }
                }
                return null;
            }
        };
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
            return title;
        }

        ReplaceSingleConstructFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class ReplaceMultipleConstructFix implements LocalQuickFix {
        private static final String title = "Use return instead";

        final private SmartPsiElementPointer<PsiElement> from;
        final private SmartPsiElementPointer<PsiElement> to;
        final String replacement;

        ReplaceMultipleConstructFix(@NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(from.getProject());

            this.from        = factory.createSmartPsiElementPointer(from);
            this.to          = factory.createSmartPsiElementPointer(to);
            this.replacement = replacement;
        }

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

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement from = this.from.getElement();
            final PsiElement to   = this.to.getElement();
            if (from != null && to != null && !project.isDisposed()) {
                final String code = this.replacement + ';';
                if (from == to) {
                    final boolean wrap       = from instanceof If && from.getParent() instanceof Else;
                    final String replacement = wrap ? "{ " + this.replacement + "; }" : this.replacement + ";";
                    from.replace(PhpPsiElementFactory.createStatement(project, replacement));
                } else {
                    final PsiElement parent = from.getParent();
                    parent.addBefore(PhpPsiElementFactory.createStatement(project, code), from);
                    parent.deleteChildRange(from, to);
                }
            }
        }
    }
}