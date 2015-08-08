package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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
    private static final String strForBehavesAsForeach  = "Probably foreach can be used instead (easier to read and support)";

    @NotNull
    public String getShortName() {
        return "ForeachInvariantsInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
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

                // find first variable initialized with null
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
                        // increment on aour variable
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
                Collection<ArrayAccessExpression> indexStatements = PsiTreeUtil.findChildrenOfType(body, ArrayAccessExpression.class);
                for (ArrayAccessExpression offset : indexStatements) {
                    if (
                        null != offset.getIndex() &&
                        offset.getIndex().getValue() instanceof Variable &&
                        PsiEquivalenceUtil.areElementsEquivalent(variable, offset.getIndex().getValue())
                    ) {
                        return true;
                    }
                }
                indexStatements.clear();

                return false;
            }
        };
    }
}