package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IfReturnReturnSimplificationInspector extends BasePhpInspection {
    private static final String messagePattern = "The construct can be replaced with '%s'.";

    @NotNull
    public String getShortName() {
        return "IfReturnReturnSimplificationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If statement) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(statement))              { return; }

                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                if (condition != null && this.isTargetCondition(condition) && statement.getElseIfBranches().length == 0) {
                    final Couple<Couple<PsiElement>> fragments = this.extract(statement);
                    final PsiElement firstValue                = fragments.second.first;
                    final PsiElement secondValue               = fragments.second.second;

                    /* if 2nd return found, check more pattern matches */
                    if (secondValue != null) {
                        final boolean isDirect  = PhpLanguageUtil.isTrue(firstValue) && PhpLanguageUtil.isFalse(secondValue);
                        final boolean isReverse = PhpLanguageUtil.isTrue(secondValue) && PhpLanguageUtil.isFalse(firstValue);
                        if (isDirect || isReverse) {
                            /* false-positives: if-return if-return return - code style */
                            if (statement.getElseBranch() == null) {
                                final PsiElement before = statement.getPrevPsiSibling();
                                if (before instanceof If && !ExpressionSemanticUtil.hasAlternativeBranches((If) before)) {
                                    final GroupStatement prevBody = ExpressionSemanticUtil.getGroupStatement(before);
                                    if (prevBody != null && ExpressionSemanticUtil.getLastStatement(prevBody) instanceof PhpReturn) {
                                        return;
                                    }
                                }
                            }

                            /* final reporting step */
                            final String replacement;
                            if (isReverse) {
                                if (condition instanceof UnaryExpression) {
                                    PsiElement extracted = ((UnaryExpression) condition).getValue();
                                    extracted            = ExpressionSemanticUtil.getExpressionTroughParenthesis(extracted);
                                    replacement          = String.format("return %s", extracted == null ? "" : extracted.getText());
                                } else {
                                    replacement = String.format("return !(%s)", condition.getText());
                                }
                            } else {
                                replacement = String.format("return %s", condition.getText());
                            }
                            holder.registerProblem(
                                    statement.getFirstChild(),
                                    String.format(messagePattern, replacement),
                                    new SimplifyFix(fragments.first.first, fragments.first.second, replacement)
                            );
                        }
                    }
                }
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
                            PsiElement elseLast         = null;
                            final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(statement.getElseBranch());
                            if (elseBody != null && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                                elseLast = this.extractCandidate(ExpressionSemanticUtil.getLastStatement(elseBody));
                            }

                            /* if - return-bool - else - return bool */
                            if (ifLast instanceof PhpReturn && elseLast instanceof PhpReturn) {
                                final PhpReturn first  = (PhpReturn) ifLast;
                                final PhpReturn second = (PhpReturn) elseLast;
                                result = new Couple<>(new Couple<>(statement, statement), new Couple<>(first.getArgument(), second.getArgument()));
                            }
                            /* if - assign - else - assign - return */
                            else if (ifLast instanceof AssignmentExpression && elseLast instanceof AssignmentExpression && ifNext instanceof PhpReturn) {
                                final AssignmentExpression ifAssignment   = (AssignmentExpression) ifLast;
                                final AssignmentExpression elseAssignment = (AssignmentExpression) elseLast;
                                final PsiElement ifContainer              = ifAssignment.getVariable();
                                final PsiElement elseContainer            = elseAssignment.getVariable();
                                final PsiElement returnedValue            = ((PhpReturn) ifNext).getArgument();
                                if (ifContainer instanceof Variable && elseContainer instanceof Variable && returnedValue instanceof Variable) {
                                    final boolean isTarget = OpenapiEquivalenceUtil.areEqual(ifContainer, elseContainer) &&
                                                             OpenapiEquivalenceUtil.areEqual(elseContainer, returnedValue);
                                    if (isTarget) {
                                        result = new Couple<>(
                                                new Couple<>(statement, ifNext),
                                                new Couple<>(ifAssignment.getValue(), elseAssignment.getValue())
                                        );
                                    }
                                }
                            }
                        } else {
                            /* assign - if - assign - return */
                            if (ifPrevious instanceof AssignmentExpression && ifLast instanceof AssignmentExpression && ifNext instanceof PhpReturn) {
                                final AssignmentExpression previousAssignment = (AssignmentExpression) ifPrevious;
                                final AssignmentExpression ifAssignment       = (AssignmentExpression) ifLast;
                                final PsiElement previousContainer            = previousAssignment.getVariable();
                                final PsiElement ifContainer                  = ifAssignment.getVariable();
                                final PsiElement returnedValue                = ((PhpReturn) ifNext).getArgument();
                                if (previousContainer instanceof Variable && ifContainer instanceof Variable && returnedValue instanceof Variable) {
                                    final boolean isTarget = OpenapiEquivalenceUtil.areEqual(previousContainer, ifContainer) &&
                                                             OpenapiEquivalenceUtil.areEqual(ifContainer, returnedValue);
                                    if (isTarget) {
                                        result = new Couple<>(
                                                new Couple<>(ifPrevious.getParent(), ifNext),
                                                new Couple<>(ifAssignment.getValue(), previousAssignment.getValue())
                                        );
                                    }
                                }
                            } else if (ifLast instanceof PhpReturn && ifNext instanceof PhpReturn) {
                                final PsiElement lastReturnedValue = ((PhpReturn) ifNext).getArgument();
                                /* assign - if - return - return */
                                if (lastReturnedValue instanceof Variable && ifPrevious instanceof AssignmentExpression) {
                                    final AssignmentExpression previousAssignment = (AssignmentExpression) ifPrevious;
                                    final PsiElement previousContainer            = previousAssignment.getVariable();
                                    if (previousContainer instanceof Variable) {
                                        final boolean isTarget = OpenapiEquivalenceUtil.areEqual(previousContainer, lastReturnedValue);
                                        if (isTarget) {
                                            result = new Couple<>(
                                                    new Couple<>(ifPrevious.getParent(), ifNext),
                                                    new Couple<>(((PhpReturn) ifLast).getArgument(), previousAssignment.getValue())
                                            );
                                        }
                                    }
                                }
                                /* if - return - return */
                                else {
                                    result = new Couple<>(
                                            new Couple<>(statement, ifNext),
                                            new Couple<>(((PhpReturn) ifLast).getArgument(), lastReturnedValue)
                                    );
                                }
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
                            final PsiElement value = assignment.getValue();
                            if (PhpLanguageUtil.isBoolean(value)) {
                                return assignment;
                            }
                        }
                    }
                }
                return null;
            }

            private boolean isTargetCondition(@NotNull PsiElement condition) {
                if (condition instanceof BinaryExpression) {
                    return true;
                } else if (condition instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) condition;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opNOT)) {
                        final PsiElement argument = ExpressionSemanticUtil.getExpressionTroughParenthesis(unary.getValue());
                        if (argument instanceof FunctionReference) {
                            return this.isTargetFunction((FunctionReference) argument);
                        }
                    }
                } else if (condition instanceof FunctionReference) {
                    return this.isTargetFunction((FunctionReference) condition);
                }
                return false;
            }

            private boolean isTargetFunction(final FunctionReference reference) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                if (resolved instanceof Function) {
                    final Function function = (Function) resolved;
                    if (OpenapiElementsUtil.getReturnType(function) != null) {
                        final PhpType returnType = function.getType();
                        return returnType.size() == 1 && returnType.equals(PhpType.BOOLEAN);
                    }
                }
                return false;
            }
        };
    }

    private static final class SimplifyFix implements LocalQuickFix {
        private static final String title = "Use return instead";

        final private SmartPsiElementPointer<PsiElement> from;
        final private SmartPsiElementPointer<PsiElement> to;
        final String replacement;

        SimplifyFix(@NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
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
                    from.replace(PhpPsiElementFactory.createPhpPsiFromText(project, PhpReturn.class, code));
                } else {
                    final PsiElement parent = from.getParent();
                    parent.addBefore(PhpPsiElementFactory.createPhpPsiFromText(project, PhpReturn.class, code), from);
                    parent.deleteChildRange(from, to);
                }
            }
        }
    }
}