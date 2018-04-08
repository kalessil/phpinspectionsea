package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IllusionOfChoiceInspector extends BasePhpInspection {
    private static final String messageSameValueConditional = "...";
    private static final String messageSameValueTernary     = "...";

    static private final Set<IElementType> targetOperations = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opIDENTICAL);
        targetOperations.add(PhpTokenTypes.opNOT_IDENTICAL);
        targetOperations.add(PhpTokenTypes.opEQUAL);
        targetOperations.add(PhpTokenTypes.opNOT_EQUAL);
        targetOperations.add(PhpTokenTypes.opLESS);
        targetOperations.add(PhpTokenTypes.opLESS_OR_EQUAL);
        targetOperations.add(PhpTokenTypes.opGREATER);
        targetOperations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
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
                final PsiElement condition = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getCondition());
                if (condition instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) condition;
                    if (targetOperations.contains(binary.getOperationType())) {
                        final PsiElement trueVariant  = expression.getTrueVariant();
                        final PsiElement falseVariant = expression.getFalseVariant();
                        if (trueVariant != null && falseVariant != null) {
                            this.analyze(binary, trueVariant, falseVariant);
                        }
                    }
                }
            }

            @Override
            public void visitPhpIf(@NotNull If expression) {
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
                                        this.analyze(binary, ifReturnValue, elseReturnValue);
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
                                        this.analyze(binary, ifReturnValue, nextReturnValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void analyze(@NotNull PsiElement target, @NotNull PsiElement trueVariant, @NotNull PsiElement falseVariant) {
                if (OpeanapiEquivalenceUtil.areEqual(trueVariant, falseVariant)) {
                    final boolean isConditional = falseVariant.getParent() instanceof PhpReturn;
                    holder.registerProblem(falseVariant, isConditional ? messageSameValueConditional : messageSameValueTernary);
                } else {
                    /* TODO: check if the construct makes sense at all */
                }
            }
        };
    }
}
