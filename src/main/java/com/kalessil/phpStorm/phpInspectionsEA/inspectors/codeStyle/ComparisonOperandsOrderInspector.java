package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationConfiguration;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ComparisonOperandsOrderInspector extends BasePhpInspection {
    private static final String messageUseYoda    = "Yoda conditions style should be used instead.";
    private static final String messageUseRegular = "Regular conditions style should be used instead.";

    @NotNull
    public String getShortName() {
        return "ComparisonOperandsOrderInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpBinaryExpression(@NotNull BinaryExpression expression) {
                final IElementType operator = expression.getOperationType();
                if (operator != null && OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                    final PsiElement left  = expression.getLeftOperand();
                    final PsiElement right = expression.getRightOperand();
                    if (left != null && right != null) {
                        final boolean isLeftConstant  = left instanceof StringLiteralExpression ||
                                                        left instanceof ConstantReference ||
                                                        OpenapiTypesUtil.isNumber(left);
                        final boolean isRightConstant = right instanceof StringLiteralExpression ||
                                                        right instanceof ConstantReference ||
                                                        OpenapiTypesUtil.isNumber(right);
                        if (isLeftConstant != isRightConstant) {
                            final boolean isRegular = ComparisonStyle.isRegular();
                            if (isRightConstant && !isRegular) {
                                problemsHolder.registerProblem(expression, messageUseYoda, new TheLocalFix());
                            }
                            if (isLeftConstant && isRegular) {
                                problemsHolder.registerProblem(expression, messageUseRegular, new TheLocalFix());
                            }
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addHyperlink("Setup Yoda or Regular style...", EAUltimateApplicationConfiguration.class)
        );
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Reorder arguments";

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
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof BinaryExpression && !project.isDisposed()) {
                final BinaryExpression expression = (BinaryExpression) target;
                final PsiElement left             = expression.getLeftOperand();
                final PsiElement right            = expression.getRightOperand();
                if (left != null && right != null) {
                    final PsiElement leftCopy = left.copy();
                    left.replace(right);
                    right.replace(leftCopy);
                }
            }
        }
    }
}
