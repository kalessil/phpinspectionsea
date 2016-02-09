package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ForeachInvariantsInspector extends BasePhpInspection {
    private static final String strForBehavesAsForeach   = "Foreach can probably be used instead (easier to read and support; ensure a string is not iterated)";
    private static final String strEachBehavesAsForeach  = "Foreach should be used instead (performance improvements)";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMultiassignmentExpression(MultiassignmentExpression multiassignmentExpression) {
                PhpPsiElement values = multiassignmentExpression.getValue();
                if (values instanceof PhpExpressionImpl) {
                    values = ((PhpExpressionImpl) values).getValue();
                }

                if (values instanceof FunctionReference && !(values instanceof MethodReference)) {
                    final FunctionReference eachCandidate = (FunctionReference) values;
                    final String function                 = eachCandidate.getName();
                    if (!StringUtil.isEmpty(function) && function.equals("each")) {
                        final PsiElement parent = multiassignmentExpression.getParent();
                        if (parent instanceof While || parent instanceof For) {
                            holder.registerProblem(parent.getFirstChild(), strEachBehavesAsForeach, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }

            public void visitPhpFor(For forStatement) {
                if (isForeachAnalog(forStatement)) {
                    /* report the issue */
                    holder.registerProblem(forStatement.getFirstChild(), strForBehavesAsForeach, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            private boolean isForeachAnalog(@NotNull For expression) {
                // group statement needed
                GroupStatement body = ExpressionSemanticUtil.getGroupStatement(expression);
                if (null == body) {
                    return false;
                }

                // find first variable initialized with 0
                Variable variable = null;
                for (PhpPsiElement init : expression.getInitialExpressions()) {
                    if (!(init instanceof AssignmentExpressionImpl)) {
                        continue;
                    }

                    AssignmentExpressionImpl intiCasted = (AssignmentExpressionImpl) init;
                    if (intiCasted.getValue() instanceof PhpExpressionImpl) {
                        PhpExpressionImpl initValue = (PhpExpressionImpl) intiCasted.getValue();
                        //noinspection ConstantConditions
                        if (initValue.getText().equals("0") && intiCasted.getVariable() instanceof Variable) {
                            variable = (Variable) intiCasted.getVariable();
                            break;
                        }
                    }
                }
                if (null == variable) {
                    return false;
                }

                // check if variable incremented
                boolean isVariableIncremented = false;
                for (PhpPsiElement repeat : expression.getRepeatedExpressions()) {
                    if (!(repeat instanceof UnaryExpressionImpl)) {
                        continue;
                    }

                    UnaryExpressionImpl repeatCasted = (UnaryExpressionImpl) repeat;
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
                            PhpPsiUtil.isOfType(comparedElement.getFirstChild(), PhpTokenTypes.DECIMAL_INTEGER)
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