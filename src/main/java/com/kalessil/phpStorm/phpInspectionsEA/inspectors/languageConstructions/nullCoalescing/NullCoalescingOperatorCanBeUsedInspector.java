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
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
                if (SUGGEST_SIMPLIFYING_TERNARIES && ! expression.isShort() && PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP700)) {
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                    if (condition != null) {
                        final PsiElement extracted = this.getTargetCondition(condition);
                        if (extracted != null) {
                            final PsiElement firstValue  = expression.getTrueVariant();
                            final PsiElement secondValue = expression.getFalseVariant();
                            if (firstValue != null && secondValue != null) {
                                final String replacement = this.generateReplacement(condition, extracted, firstValue, secondValue);
                                if (replacement != null) {
                                    holder.registerProblem(
                                            expression,
                                            String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                            new ReplaceSingleConstructFix(replacement)
                                    );
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If statement) {
                final Project project = holder.getProject();
                if (SUGGEST_SIMPLIFYING_IFS && PhpLanguageLevel.get(project).atLeast(PhpLanguageLevel.PHP700)) {
                    final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(statement.getCondition());
                    if (condition != null && statement.getElseIfBranches().length == 0) {
                        final PsiElement extracted = this.getTargetCondition(condition);
                        if (extracted != null) {
                            final Couple<Couple<PsiElement>> fragments = this.extract(statement);
                            final PsiElement firstValue                = fragments.second.first;
                            final PsiElement secondValue               = fragments.second.second;
                            if (firstValue != null) {
                                final String coalescing = this.generateReplacement(condition, extracted, firstValue, secondValue);
                                if (coalescing != null) {
                                    final PsiElement context = firstValue.getParent();
                                    if (context instanceof PhpReturn) {
                                        final String replacement = String.format("return %s", coalescing);
                                        holder.registerProblem(
                                                statement.getFirstChild(),
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                new ReplaceMultipleConstructFix(project, fragments.first.first, fragments.first.second, replacement)
                                        );
                                    } else if (context instanceof AssignmentExpression) {
                                        final PsiElement container = ((AssignmentExpression) context).getVariable();
                                        final String replacement   = String.format("%s = %s", container.getText(), coalescing);
                                        holder.registerProblem(
                                                statement.getFirstChild(),
                                                String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                new ReplaceMultipleConstructFix(project, fragments.first.first, fragments.first.second, replacement)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean wrap(@Nullable PsiElement expression) {
                if (expression instanceof TernaryExpression || expression instanceof AssignmentExpression) {
                    return true;
                } else if (expression instanceof BinaryExpression) {
                    return ((BinaryExpression) expression).getOperationType() != PhpTokenTypes.opCOALESCE;
                }
                return false;
            }

            @Nullable
            private String generateReplacement(
                    @NotNull PsiElement condition,
                    @NotNull PsiElement extracted,
                    @NotNull PsiElement first,
                    @Nullable PsiElement second
            ) {
                String coalescing = null;
                if (extracted instanceof PhpIsset) {
                    coalescing = this.generateReplacementForIsset(condition, (PhpIsset) extracted, first, second);
                } else if (extracted instanceof PhpEmpty) {
                    coalescing = this.generateReplacementForPropertyAccess(condition, (PhpEmpty) extracted, first, second);
                } else if (extracted instanceof Variable || extracted instanceof ArrayAccessExpression || extracted instanceof FieldReference) {
                    coalescing = this.generateReplacementForPropertyAccess(condition, extracted, first, second);
                } else if (extracted instanceof FunctionReference) {
                    if (second != null) {
                        coalescing = this.generateReplacementForExists(condition, (FunctionReference) extracted, first, second);
                    }
                } else if (extracted instanceof BinaryExpression) {
                    if (second != null) {
                        coalescing = this.generateReplacementForIdentity(condition, (BinaryExpression) extracted, first, second);
                    }
                }
                return coalescing;
            }

            @Nullable
            private String generateReplacementForExists(
                    @NotNull PsiElement condition,
                    @NotNull FunctionReference extracted,
                    @NotNull PsiElement first,
                    @NotNull PsiElement second
            ) {
                final PsiElement[] arguments = extracted.getParameters();
                if (arguments.length == 2) {
                    final boolean expectsToBeSet = condition == extracted;
                    final PsiElement candidate   = expectsToBeSet ? first : second;
                    final PsiElement alternative = expectsToBeSet ? second : first;
                    if (candidate instanceof ArrayAccessExpression && PhpLanguageUtil.isNull(alternative)) {
                        final ArrayAccessExpression access = (ArrayAccessExpression) candidate;
                        final PsiElement container         = access.getValue();
                        if (container != null && OpenapiEquivalenceUtil.areEqual(container, arguments[1])) {
                            final ArrayIndex index = access.getIndex();
                            if (index != null) {
                                final PsiElement key = index.getValue();
                                if (key != null && OpenapiEquivalenceUtil.areEqual(key, arguments[0])) {
                                    return String.format(
                                            "%s ?? %s",
                                            String.format(this.wrap(candidate) ? "(%s)" : "%s", candidate.getText()),
                                            String.format(this.wrap(alternative) ? "(%s)" : "%s", alternative.getText())
                                    );
                                }
                            }
                        }
                    }
                }
                return null;
            }

            @Nullable
            private String generateReplacementForIdentity(
                    @NotNull PsiElement condition,
                    @NotNull BinaryExpression extracted,
                    @NotNull PsiElement first,
                    @NotNull PsiElement second
            ) {
                PsiElement subject = extracted.getLeftOperand();
                if (PhpLanguageUtil.isNull(subject)) {
                    subject = extracted.getRightOperand();
                }
                if (subject != null) {
                    final IElementType operator  = extracted.getOperationType();
                    final boolean expectsToBeSet = (operator == PhpTokenTypes.opNOT_IDENTICAL && condition == extracted) ||
                                                   (operator == PhpTokenTypes.opIDENTICAL && condition != extracted);
                    final PsiElement candidate   = expectsToBeSet ? first : second;
                    if (OpenapiEquivalenceUtil.areEqual(candidate, subject)) {
                        final PsiElement alternative = expectsToBeSet ? second : first;
                        return String.format(
                                "%s ?? %s",
                                String.format(this.wrap(candidate) ? "(%s)" : "%s", candidate.getText()),
                                String.format(this.wrap(alternative) ? "(%s)" : "%s", alternative.getText())
                        );
                    }
                }
                return null;
            }

            @Nullable
            private String generateReplacementForIsset(
                    @NotNull PsiElement condition,
                    @NotNull PhpIsset extracted,
                    @NotNull PsiElement first,
                    @Nullable PsiElement second
            ) {
                final PsiElement subject = extracted.getVariables()[0];
                if (subject != null) {
                    final boolean expectsToBeSet = condition == extracted;
                    final PsiElement candidate   = expectsToBeSet ? first : second;
                    if (candidate != null && OpenapiEquivalenceUtil.areEqual(candidate, subject)) {
                        final PsiElement alternative = expectsToBeSet ? second : first;
                        return String.format(
                                "%s ?? %s",
                                String.format(this.wrap(candidate) ? "(%s)" : "%s", candidate.getText()),
                                String.format(this.wrap(alternative) ? "(%s)" : "%s", alternative == null ? "null" : alternative.getText())
                        );
                    }
                }
                return null;
            }

            @Nullable
            private String generateReplacementForPropertyAccess(
                    @NotNull PsiElement condition,
                    @NotNull PsiElement extracted,
                    @NotNull PsiElement first,
                    @Nullable PsiElement second
            ) {
                final boolean expectsToBeNotEmpty = condition == extracted;
                final PsiElement candidate        = expectsToBeNotEmpty ? first : second;
                if (candidate instanceof FieldReference) {
                    final FieldReference fieldReference = (FieldReference) candidate;
                    final PsiElement base               = fieldReference.getClassReference();
                    if (base != null && OpenapiEquivalenceUtil.areEqual(extracted, base)) {
                        final PhpType resolved = OpenapiResolveUtil.resolveType(fieldReference, holder.getProject());
                        if (resolved != null && ! resolved.filterUnknown().isEmpty()) {
                            final PsiElement alternative = expectsToBeNotEmpty ? second : first;
                            final boolean isNullable     = resolved.filterUnknown().getTypes().stream()
                                    .map(Types::getType)
                                    .anyMatch(t -> t.equals(Types.strNull));
                            if (! isNullable || (alternative == null || PhpLanguageUtil.isNull(alternative))) {
                                return String.format(
                                        "%s ?? %s",
                                        String.format(this.wrap(fieldReference) ? "(%s)" : "%s", fieldReference.getText()),
                                        String.format(this.wrap(alternative) ? "(%s)" : "%s", alternative == null ? "null" : alternative.getText())
                                );
                            }
                        }
                    }
                }
                return null;
            }

            @Nullable
            private String generateReplacementForPropertyAccess(
                    @NotNull PsiElement condition,
                    @NotNull PhpEmpty extracted,
                    @NotNull PsiElement first,
                    @Nullable PsiElement second
            ) {
                final PsiElement subject = extracted.getVariables()[0];
                if (subject != null) {
                    final boolean expectsToBeNotEmpty = condition != extracted;
                    final PsiElement candidate        = expectsToBeNotEmpty ? first : second;
                    if (candidate instanceof FieldReference) {
                        final PsiElement reference = ((FieldReference) candidate).getClassReference();
                        if (reference != null && OpenapiEquivalenceUtil.areEqual(subject, reference)) {
                            final PsiElement alternative = expectsToBeNotEmpty ? second : first;
                            return String.format(
                                    "%s ?? %s",
                                    String.format(this.wrap(candidate) ? "(%s)" : "%s", candidate.getText()),
                                    String.format(this.wrap(alternative) ? "(%s)" : "%s", alternative == null ? "null" : alternative.getText())
                            );
                        }
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
                if (condition instanceof Variable || condition instanceof ArrayAccessExpression || condition instanceof FieldReference) {
                    return condition;
                } else if (condition instanceof PhpIsset) {
                    final PhpIsset isset = (PhpIsset) condition;
                    if (isset.getVariables().length == 1) {
                        return condition;
                    }
                } else if (condition instanceof PhpEmpty) {
                    final PhpEmpty empty = (PhpEmpty) condition;
                    if (empty.getVariables().length == 1) {
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
                                    /* false-positives: assignment by value */
                                    if (isTarget && ! OpenapiTypesUtil.isAssignmentByReference(previousAssignment)) {
                                        final PsiElement previousValue = previousAssignment.getValue();
                                        if (! (previousValue instanceof AssignmentExpression)) {
                                            /* false-positives: assignment of processed container value */
                                            final boolean isContainerProcessing = PsiTreeUtil.findChildrenOfType(previousValue, previousContainer.getClass()).stream()
                                                    .anyMatch(c -> OpenapiEquivalenceUtil.areEqual(c, previousContainer));
                                            if (! isContainerProcessing) {
                                                result = new Couple<>(
                                                        new Couple<>(ifPrevious.getParent(), statement),
                                                        new Couple<>(ifAssignment.getValue(), previousValue)
                                                );
                                            }
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
                            /* if - return - [end-of-function] */
                            else if (ifLast instanceof PhpReturn && ifNext == null && statement.getNextPsiSibling() == null) {
                                final boolean isInFunction = statement.getParent().getParent() instanceof Function;
                                if (isInFunction) {
                                    result = new Couple<>(
                                            new Couple<>(statement, statement),
                                            new Couple<>(((PhpReturn) ifLast).getArgument(), null)
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        ReplaceSingleConstructFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class ReplaceMultipleConstructFix implements LocalQuickFix {
        private static final String title = "Replace with null coalescing operator";

        final private SmartPsiElementPointer<PsiElement> from;
        final private SmartPsiElementPointer<PsiElement> to;
        final String replacement;

        ReplaceMultipleConstructFix(@NotNull Project project, @NotNull PsiElement from, @NotNull PsiElement to, @NotNull String replacement) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.from        = factory.createSmartPsiElementPointer(from);
            this.to          = factory.createSmartPsiElementPointer(to);
            this.replacement = replacement;
        }

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