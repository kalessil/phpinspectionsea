package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.android.annotations.Nullable;
import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ForeachInvariantsInspector extends BasePhpInspection {
    private static final String foreachInvariant = "Foreach can probably be used instead (easier to read and support; ensure a string is not iterated).";
    private static final String eachFunctionUsed = "Foreach should be used instead (8x faster).";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFor(@NotNull For forStatement) {
                if (this.isForeachAnalog(forStatement)) {
                    holder.registerProblem(forStatement.getFirstChild(), foreachInvariant);
                }
            }

            @Override
            public void visitPhpMultiassignmentExpression(@NotNull MultiassignmentExpression assignmentExpression) {
                PsiElement value = assignmentExpression.getValue();
                if (OpenapiTypesUtil.is(value, PhpElementTypes.EXPRESSION)) {
                    value = value.getFirstChild();
                }

                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final String functionName = ((FunctionReference) value).getName();
                    if (functionName != null && functionName.equals("each")) {
                        final PsiElement parent = assignmentExpression.getParent();
                        if (parent instanceof While || parent instanceof For) {
                            holder.registerProblem(parent.getFirstChild(), eachFunctionUsed, ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }

            @Nullable
            private PsiElement getCounterVariable(@NotNull For expression) {
                PsiElement result = null;
                for (final PhpPsiElement init : expression.getInitialExpressions()) {
                    if (OpenapiTypesUtil.isAssignment(init)) {
                        final AssignmentExpression assignment = (AssignmentExpression) init;
                        final PsiElement value                = assignment.getValue();
                        final PsiElement variable             = assignment.getVariable();
                        if (value != null && variable instanceof Variable && value.getText().equals("0")) {
                            result = variable;
                            break;
                        }
                    }
                }
                return result;
            }

            private boolean isCounterVariableIncremented(@NotNull For expression, @NotNull Variable variable) {
                boolean result = false;
                for (final PsiElement repeat : expression.getRepeatedExpressions()) {
                    if (repeat instanceof UnaryExpression) {
                        final UnaryExpression incrementCandidate = (UnaryExpression) repeat;
                        final PsiElement argument                = incrementCandidate.getValue();
                        if (
                            OpenapiTypesUtil.is(incrementCandidate.getOperation(), PhpTokenTypes.opINCREMENT) &&
                            argument != null && PsiEquivalenceUtil.areElementsEquivalent(variable, argument)
                        ) {
                            result = true;
                            break;
                        }
                    }
                }
                return result;
            }

            private boolean isForeachAnalog(@NotNull For expression) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                if (body == null || ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                    return false;
                }
                final PsiElement variable = this.getCounterVariable(expression);
                if (variable == null || !this.isCounterVariableIncremented(expression, (Variable) variable)) {
                    return false;
                }

                // find usages as index
                boolean isUsedAsIndex = false;
                Collection<ArrayAccessExpression> indexStatements = PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class);
                // TODO: strings, ++variable
                for (ArrayAccessExpression offset : indexStatements) {
                    if (
                        null != offset.getIndex() &&
                        offset.getIndex().getValue() instanceof Variable &&
                        PsiEquivalenceUtil.areElementsEquivalent(variable, offset.getIndex().getValue())
                    ) {
                        isUsedAsIndex = true;
                        break;
                    }
                }
                indexStatements.clear();
                if (!isUsedAsIndex) {
                    return false;
                }

                // ensure not compared with fixed number
                boolean isComparedNotProperExpression = false;
                boolean isBinaryExpression = false;
                for (PhpPsiElement condition : expression.getConditionalExpressions()) {
                    isBinaryExpression = condition instanceof BinaryExpression;
                    if (!isBinaryExpression) {
                        continue;
                    }

                    BinaryExpression conditionCasted = (BinaryExpression) condition;

                    // get compared value
                    PsiElement comparedElement = null;
                    if (
                        conditionCasted.getLeftOperand() instanceof Variable &&
                        PsiEquivalenceUtil.areElementsEquivalent(variable, conditionCasted.getLeftOperand())
                    ) {
                        comparedElement = conditionCasted.getRightOperand();
                    }
                    if (
                        conditionCasted.getRightOperand() instanceof Variable &&
                        PsiEquivalenceUtil.areElementsEquivalent(variable, conditionCasted.getRightOperand())
                    ) {
                        comparedElement = conditionCasted.getLeftOperand();
                    }
                    if (comparedElement instanceof AssignmentExpression) {
                        isComparedNotProperExpression = true;
                        continue;
                    }

                    // stop analysis if unexpected expression used for comparison
                    if (
                        null != comparedElement &&
                        (
                            comparedElement instanceof BinaryExpression      || // e.g. mathematical operations
                            comparedElement instanceof FunctionReference     || // first the function needs to be relocated
                            comparedElement instanceof ArrayAccessExpression || // we can not analyze this anyway
                            OpenapiTypesUtil.is(comparedElement.getFirstChild(), PhpTokenTypes.DECIMAL_INTEGER)
                        )
                    ) {
                        isComparedNotProperExpression = true;
                        break;
                    }
                }

                return isBinaryExpression && !isComparedNotProperExpression;
            }
        };
    }
}