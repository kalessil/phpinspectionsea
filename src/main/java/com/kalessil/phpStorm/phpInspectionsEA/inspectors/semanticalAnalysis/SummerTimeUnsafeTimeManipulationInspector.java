package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SummerTimeUnsafeTimeManipulationInspector extends BasePhpInspection {
    private static final String message = "Consider using \\DateTime for DST safe date/time manipulation.";

    @NotNull
    public String getShortName() {
        return "SummerTimeUnsafeTimeManipulationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operationType = expression.getOperationType();
                if (
                    operationType == PhpTokenTypes.opMUL ||
                    operationType == PhpTokenTypes.opDIV ||
                    operationType == PhpTokenTypes.opREM ||
                    operationType == PhpTokenTypes.opMINUS ||
                    operationType == PhpTokenTypes.opPLUS
                ) {
                    final PsiElement left  = expression.getLeftOperand();
                    final PsiElement right = expression.getRightOperand();
                    if (right != null && this.isTargetMagicNumber(right) && this.isTargetContext(right)) {
                        holder.registerProblem(expression, message);
                    } else if (left != null && this.isTargetMagicNumber(left) && this.isTargetContext(left)) {
                        holder.registerProblem(expression, message);
                    }
                }
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                final IElementType operationType = expression.getOperationType();
                if (
                    operationType == PhpTokenTypes.opMUL_ASGN ||
                    operationType == PhpTokenTypes.opDIV_ASGN ||
                    operationType == PhpTokenTypes.opREM_ASGN ||
                    operationType == PhpTokenTypes.opMINUS_ASGN ||
                    operationType == PhpTokenTypes.opPLUS_ASGN
                ) {
                    final PsiElement value = expression.getValue();
                    if (value !=null && this.isTargetMagicNumber(value)) {
                        holder.registerProblem(expression, message);
                    }
                }
            }

            private boolean isTargetContext(@NotNull PsiElement magicNumber) {
                boolean result = magicNumber.textMatches("86400");
                if (!result) {
                    PsiElement expression = magicNumber.getParent();
                    while (expression instanceof ParenthesizedExpression || expression instanceof BinaryExpression) {
                        expression = expression.getParent();
                    }
                    for (final PsiElement candidate : PsiTreeUtil.findChildrenOfType(expression, PhpExpression.class)) {
                        if (OpenapiTypesUtil.isNumber(candidate)) {
                            final String text = candidate.getText();
                            if (text.equals("60") || text.endsWith("3600")) {
                                result = true;
                                break;
                            }
                        }
                    }
                }
                return result;
            }

            private boolean isTargetMagicNumber(@NotNull PsiElement candidate) {
                boolean result = false;
                if (OpenapiTypesUtil.isNumber(candidate)) {
                    result = candidate.textMatches("24") || candidate.textMatches("86400");
                }
                return result;
            }
       };
    }
}