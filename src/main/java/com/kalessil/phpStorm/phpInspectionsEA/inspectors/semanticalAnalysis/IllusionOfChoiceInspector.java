package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class IllusionOfChoiceInspector extends BasePhpInspection {
    private static final String messageSameValueConditional = "Same value gets returned by the alternative return. It's possible to simplify the construct.";
    private static final String messageDegradedConditional  = "Actually the same value gets returned by the alternative return. It's possible to simplify the construct.";
    private static final String messageDegradedTernary      = "Actually the same value is in the alternative variant. It's possible to simplify the construct.";

    static private final Set<IElementType> targetOperations = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opEQUAL);
        targetOperations.add(PhpTokenTypes.opNOT_EQUAL);
    }

    @NotNull
    public String getShortName() {
        return "IllusionOfChoiceInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTernaryExpression(@NotNull TernaryExpression expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    if (targetOperations.contains(binary.getOperationType())) {
                        final PsiElement trueVariant  = expression.getTrueVariant();
                        final PsiElement falseVariant = expression.getFalseVariant();
                        if (trueVariant != null && falseVariant != null) {
                            this.analyze(binary, trueVariant, falseVariant, expression, expression);
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                final PsiElement condition = expression.getCondition();
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    if (targetOperations.contains(binary.getOperationType()) && expression.getElseIfBranches().length == 0) {
                        final GroupStatement ifBody = ExpressionSemanticUtil.getGroupStatement(expression);
                        final PsiElement ifLast     = ifBody == null ? null : ExpressionSemanticUtil.getLastStatement(ifBody);
                        if (ifLast instanceof PhpReturn && ExpressionSemanticUtil.countExpressionsInGroup(ifBody) == 1) {
                            final PsiElement elseStatement = expression.getElseBranch();
                            if (elseStatement != null) {
                                /* both if-else has returns only */
                                final GroupStatement elseBody = ExpressionSemanticUtil.getGroupStatement(elseStatement);
                                final PsiElement elseLast     = elseBody == null ? null : ExpressionSemanticUtil.getLastStatement(elseBody);
                                if (elseLast instanceof PhpReturn && ExpressionSemanticUtil.countExpressionsInGroup(elseBody) == 1) {
                                    final PsiElement ifReturnValue   = ExpressionSemanticUtil.getReturnValue((PhpReturn) ifLast);
                                    final PsiElement elseReturnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) elseLast);
                                    if (ifReturnValue != null && elseReturnValue != null) {
                                        this.analyze(binary, ifReturnValue, elseReturnValue, expression, expression);
                                    }
                                }
                            } else {
                                /* if has return only and the next statement is return */
                                PsiElement nextStatement = expression.getNextPsiSibling();
                                while (nextStatement instanceof PhpDocComment) {
                                    nextStatement = ((PhpDocComment) nextStatement).getNextPsiSibling();
                                }
                                if (nextStatement instanceof PhpReturn) {
                                    final PsiElement ifReturnValue   = ExpressionSemanticUtil.getReturnValue((PhpReturn) ifLast);
                                    final PsiElement nextReturnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) nextStatement);
                                    if (ifReturnValue != null && nextReturnValue != null) {
                                        this.analyze(binary, ifReturnValue, nextReturnValue, expression, nextStatement);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void analyze(
                @NotNull BinaryExpression binary,
                @NotNull PsiElement trueVariant,
                @NotNull PsiElement falseVariant,
                @NotNull PsiElement replaceFrom,
                @NotNull PsiElement replaceTo
            ) {
                if (OpenapiEquivalenceUtil.areEqual(trueVariant, falseVariant)) {
                    final boolean isConditional = falseVariant.getParent() instanceof PhpReturn;
                    if (isConditional) {
                        final String replacement = String.format("return %s", falseVariant.getText());
                        holder.registerProblem(
                                falseVariant,
                                 messageSameValueConditional,
                                new SimplifyFix(replaceFrom, replaceTo, replacement)
                        );
                    }
                    /* ternaries covered by SuspiciousTernaryOperatorInspector in this case */
                } else {
                    final PsiElement leftValue  = binary.getLeftOperand();
                    final PsiElement rightValue = binary.getRightOperand();
                    if (leftValue != null && rightValue != null) {
                        final boolean isTarget = Stream.of(leftValue, rightValue).allMatch(v ->
                                OpenapiEquivalenceUtil.areEqual(v, trueVariant) ||
                                OpenapiEquivalenceUtil.areEqual(v, falseVariant)
                        );
                        if (isTarget) {
                            final IElementType operation = binary.getOperationType();
                            final boolean isInverted    = operation == PhpTokenTypes.opNOT_IDENTICAL || operation == PhpTokenTypes.opNOT_EQUAL;
                            final PsiElement falseValue = isInverted ? trueVariant : falseVariant;
                            final boolean isConditional = falseVariant.getParent() instanceof PhpReturn;
                            final String replacement    = String.format(isConditional ? "return %s" : "%s", falseValue.getText());
                            holder.registerProblem(
                                    falseValue,
                                    isConditional ? messageDegradedConditional : messageDegradedTernary,
                                    new SimplifyFix(replaceFrom, replaceTo, replacement)
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class SimplifyFix implements LocalQuickFix {
        private static final String title = "Apply the simplification";

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
                final PsiElement replacement;
                if (this.replacement.startsWith("return ")) {
                    replacement = PhpPsiElementFactory
                            .createPhpPsiFromText(project, PhpReturn.class, this.replacement + ';');
                } else {
                    replacement = PhpPsiElementFactory
                            .createPhpPsiFromText(project, ParenthesizedExpression.class, '(' + this.replacement + ')')
                            .getArgument();
                }
                if (replacement != null) {
                    if (from == to) {
                        from.replace(replacement);
                    } else {
                        final PsiElement parent = from.getParent();
                        parent.addBefore(replacement, from);
                        parent.deleteChildRange(from, to);
                    }
                }
            }
        }
    }
}
