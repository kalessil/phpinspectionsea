package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class PrefixedIncDecrementEquivalentInspector extends BasePhpInspection {
    private static final String strProblemDescriptionIncrement = "Can be safely replaced with '++%s%'";
    private static final String strProblemDescriptionDecrement = "Can be safely replaced with '--%s%'";

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /** self assignments */
            public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression) {
                IElementType operation = expression.getOperationType();
                if (null != expression.getVariable()) {
                    if (operation == PhpTokenTypes.opPLUS_ASGN) {
                        String strMessage = strProblemDescriptionIncrement.replace("%s%", expression.getVariable().getText());
                        holder.registerProblem(expression, strMessage, ProblemHighlightType.WEAK_WARNING);
                        return;
                    }

                    if (operation == PhpTokenTypes.opMINUS_ASGN) {
                        String strMessage = strProblemDescriptionDecrement.replace("%s%", expression.getVariable().getText());
                        holder.registerProblem(expression, strMessage, ProblemHighlightType.WEAK_WARNING);
                        //return;
                    }
                }
            }

            /** assignments expressions inspection*/
            public void visitPhpAssignmentExpression(AssignmentExpression assignmentExpression) {
                PhpPsiElement variable = assignmentExpression.getVariable();
                if (null != variable && assignmentExpression.getValue() instanceof BinaryExpression) {
                    BinaryExpression value = (BinaryExpression) assignmentExpression.getValue();

                    /** operation and operands provided */
                    IElementType operation  = value.getOperationType();
                    PsiElement leftOperand  = value.getLeftOperand();
                    PsiElement rightOperand = value.getRightOperand();
                    if (null == leftOperand || null == rightOperand || null == operation) {
                        return;
                    }

                    if (operation == PhpTokenTypes.opPLUS) {
                        /** plus operation: operand position NOT important */
                        if (
                            (leftOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(rightOperand, variable)) ||
                            (rightOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable))
                        ) {
                            String strMessage = strProblemDescriptionIncrement.replace("%s%", variable.getText());
                            holder.registerProblem(assignmentExpression, strMessage, ProblemHighlightType.WEAK_WARNING);
                        }

                        return;
                    }

                    if (operation == PhpTokenTypes.opMINUS) {
                        /** minus operation: operand position IS important */
                        if (rightOperand.getText().equals("1") && PsiEquivalenceUtil.areElementsEquivalent(leftOperand, variable)) {
                            String strMessage = strProblemDescriptionDecrement.replace("%s%", variable.getText());
                            holder.registerProblem(assignmentExpression, strMessage, ProblemHighlightType.WEAK_WARNING);
                        }

                        //return;
                    }
                }
            }
        };
    }
}
