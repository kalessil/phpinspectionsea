package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ParenthesizedExpression;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SummerTimeUnsafeTimeManipulationInspector extends PhpInspection {
    private static final String message = "Consider using \\DateTime for DST safe date/time manipulation.";

    private static final Set<IElementType> targetOperations  = new HashSet<>();
    private static final Set<IElementType> targetAssignments = new HashSet<>();
    static {
        targetOperations.add(PhpTokenTypes.opMUL);
        targetOperations.add(PhpTokenTypes.opDIV);
        targetOperations.add(PhpTokenTypes.opREM);
        targetOperations.add(PhpTokenTypes.opMINUS);
        targetOperations.add(PhpTokenTypes.opPLUS);

        targetAssignments.add(PhpTokenTypes.opMUL_ASGN);
        targetAssignments.add(PhpTokenTypes.opDIV_ASGN);
        targetAssignments.add(PhpTokenTypes.opREM_ASGN);
        targetAssignments.add(PhpTokenTypes.opMINUS_ASGN);
        targetAssignments.add(PhpTokenTypes.opPLUS_ASGN);
    }

    @NotNull
    public String getShortName() {
        return "SummerTimeUnsafeTimeManipulationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (targetOperations.contains(expression.getOperationType())) {
                    final PsiElement left  = expression.getLeftOperand();
                    final PsiElement right = expression.getRightOperand();
                    if (right != null && this.isTargetMagicNumber(right) && this.isTargetContext(right)) {
                        if (!this.isTestContext(expression)) {
                            holder.registerProblem(expression, message);
                        }
                    } else if (left != null && this.isTargetMagicNumber(left) && this.isTargetContext(left)) {
                        if (!this.isTestContext(expression)) {
                            holder.registerProblem(expression, message);
                        }
                    }
                }
            }

            @Override
            public void visitPhpSelfAssignmentExpression(@NotNull SelfAssignmentExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                if (targetAssignments.contains(expression.getOperationType())) {
                    final PsiElement value = expression.getValue();
                    if (value != null && this.isTargetMagicNumber(value) && !this.isTestContext(expression)) {
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
                    result = PsiTreeUtil.findChildrenOfType(expression, PhpExpression.class).stream()
                            .filter(OpenapiTypesUtil::isNumber)
                            .anyMatch(candidate -> {
                                final String text = candidate.getText();
                                return text.equals("60") || text.endsWith("3600");
                            });
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