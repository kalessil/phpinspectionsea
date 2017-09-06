package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

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

            private boolean isForeachAnalog(@NotNull For expression) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                if (body == null) {
                    return false;
                }

                /* find first variable initialized with 0 */
                PsiElement variable = null;
                for (final PhpPsiElement init : expression.getInitialExpressions()) {
                    if (OpenapiTypesUtil.isAssignment(init)) {
                        final AssignmentExpression assignment = (AssignmentExpression) init;
                        final PsiElement value                = assignment.getValue();
                        if (value != null) {
                            final PsiElement candidateVariable = assignment.getVariable();
                            if (candidateVariable instanceof Variable && value.getText().equals("0")) {
                                variable = candidateVariable;
                                break;
                            }
                        }
                    }
                }
                if (null == variable) {
                    return false;
                }

                // check if variable incremented
                boolean isVariableIncremented = false;
                for (PhpPsiElement repeat : expression.getRepeatedExpressions()) {
                    if (!(repeat instanceof UnaryExpression)) {
                        continue;
                    }

                    final UnaryExpression repeatCasted = (UnaryExpression) repeat;
                    // operation applied to a variable
                    if (null != repeatCasted.getOperation() && repeatCasted.getFirstPsiChild() instanceof Variable) {
                        // increment on our variable
                        if (
                            repeatCasted.getOperation().getNode().getElementType() == PhpTokenTypes.opINCREMENT &&
                            PsiEquivalenceUtil.areElementsEquivalent(variable, repeatCasted.getFirstPsiChild())
                        ) {
                            isVariableIncremented = true;
                            break;
                        }
                    }
                }
                if (!isVariableIncremented) {
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