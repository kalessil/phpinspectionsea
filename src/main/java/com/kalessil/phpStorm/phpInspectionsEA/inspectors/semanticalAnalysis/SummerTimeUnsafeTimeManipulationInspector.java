package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class SummerTimeUnsafeTimeManipulationInspector extends BasePhpInspection {
    private static final String strProblemDescription = "Consider using \\DateTime for DST safe date/time manipulation";

    @NotNull
    public String getShortName() {
        return "SummerTimeUnsafeTimeManipulationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                PsiElement operation = expression.getOperation();
                PsiElement left = expression.getLeftOperand();
                PsiElement right = expression.getRightOperand();
                if (null == operation || null == left || null == right) {
                    return;
                }

                IElementType operationType = operation.getNode().getElementType();
                if (
                    operationType == PhpTokenTypes.opMUL ||
                    operationType == PhpTokenTypes.opDIV ||
                    operationType == PhpTokenTypes.opREM ||
                    operationType == PhpTokenTypes.opMINUS ||
                    operationType == PhpTokenTypes.opPLUS
                ) {
                    final boolean isOneDayNumberUsed = (
                        right.textMatches("24") || right.textMatches("86400") ||
                        (
                            !(left instanceof BinaryExpression) &&
                            (left.textMatches("24") || left.textMatches("86400"))
                        )
                    );
                    if (isOneDayNumberUsed) {
                        holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }

            public void visitPhpSelfAssignmentExpression(SelfAssignmentExpression expression) {
                PsiElement operation = expression.getOperation();
                PsiElement left = expression.getVariable();
                PsiElement right = expression.getValue();
                if (null == operation || null == left || null == right) {
                    return;
                }

                IElementType operationType = operation.getNode().getElementType();
                if (
                    operationType == PhpTokenTypes.opMUL_ASGN ||
                    operationType == PhpTokenTypes.opDIV_ASGN ||
                    operationType == PhpTokenTypes.opREM_ASGN ||
                    operationType == PhpTokenTypes.opMINUS_ASGN ||
                    operationType == PhpTokenTypes.opPLUS_ASGN
                ) {
                    if (right.textMatches("24")) {
                        holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
       };
    }
}