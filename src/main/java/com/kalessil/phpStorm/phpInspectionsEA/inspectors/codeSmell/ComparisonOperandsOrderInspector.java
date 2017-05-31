package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

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

    // Inspection options.
    public boolean PREFER_YODA_STYLE    = false;
    public boolean PREFER_REGULAR_STYLE = false;

    private static final Collection<IElementType> operations = new HashSet<>();
    static {
        operations.add(PhpTokenTypes.opEQUAL);
        operations.add(PhpTokenTypes.opNOT_EQUAL);
        operations.add(PhpTokenTypes.opIDENTICAL);
        operations.add(PhpTokenTypes.opNOT_IDENTICAL);
    }

    @NotNull
    public String getShortName() {
        return "ComparisonOperandsOrderInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(final BinaryExpression expression) {
                /* verify general structure */
                final IElementType operator = expression.getOperationType();
                final PsiElement left       = expression.getLeftOperand();
                final PsiElement right      = expression.getRightOperand();
                if (left != null && right != null && operator != null && operations.contains(operator)) {
                    final boolean isLeftConstant =
                        left instanceof StringLiteralExpression || left instanceof ConstantReference ||
                        PhpElementTypes.NUMBER == left.getNode().getElementType();
                    final boolean isRightConstant =
                        right instanceof StringLiteralExpression || right instanceof ConstantReference ||
                        PhpElementTypes.NUMBER == right.getNode().getElementType();
                    if (isLeftConstant != isRightConstant) {
                        if (PREFER_YODA_STYLE && isRightConstant) {
                            problemsHolder.registerProblem(expression, messageUseYoda, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                        }
                        if (PREFER_REGULAR_STYLE && isLeftConstant) {
                            problemsHolder.registerProblem(expression, messageUseRegular, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> component.delegateRadioCreation((radioComponent) -> {
            radioComponent.addOption("Prefer regular style", PREFER_REGULAR_STYLE, (isSelected) -> PREFER_REGULAR_STYLE = isSelected);
            radioComponent.addOption("Prefer yoda style", PREFER_YODA_STYLE, (isSelected) -> PREFER_YODA_STYLE = isSelected);
        }));
    }

    private class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Reorder arguments";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof BinaryExpression) {
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
