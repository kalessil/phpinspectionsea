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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

public class ComparisonOperandsOrderInspector extends BasePhpInspection {
    // Inspection options.
    public boolean PREFER_YODA_STYLE    = false;
    public boolean PREFER_REGULAR_STYLE = false;

    final static private String messageUseYoda    = "Yoda conditions style should be used instead";
    final static private String messageUseRegular = "Regular conditions style should be used instead";

    final static private Set<IElementType> operations   = new HashSet<>();
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
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression) {
                if (!PREFER_YODA_STYLE && !PREFER_REGULAR_STYLE) {
                    return;
                }

                /* verify general structure */
                final IElementType operator = expression.getOperationType();
                final PsiElement   left     = expression.getLeftOperand();
                final PsiElement   right    = expression.getRightOperand();
                if (null == operator || null == left || null == right || !operations.contains(operator)) {
                    return;
                }

                /* identify operands type */
                final boolean isLeftConstant =
                    left instanceof StringLiteralExpression ||
                    left instanceof ConstantReference ||
                    PhpElementTypes.NUMBER == left.getNode().getElementType();
                final boolean isRightConstant =
                    right instanceof StringLiteralExpression ||
                    right instanceof ConstantReference ||
                    PhpElementTypes.NUMBER == right.getNode().getElementType();
                if (isLeftConstant == isRightConstant) {
                    return;
                }

                if (PREFER_YODA_STYLE && isRightConstant) {
                    holder.registerProblem(expression, messageUseYoda, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                    return;
                }
                if (PREFER_REGULAR_STYLE && isLeftConstant) {
                    holder.registerProblem(expression, messageUseRegular, ProblemHighlightType.WEAK_WARNING, new TheLocalFix());
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createRadio((radioComponent) -> {
                radioComponent.createOption("Prefer yoda style", PREFER_YODA_STYLE, (isSelected) -> PREFER_YODA_STYLE = isSelected);
                radioComponent.createOption("Prefer regular style", PREFER_REGULAR_STYLE, (isSelected) -> PREFER_REGULAR_STYLE = isSelected);
            });
        });
    }

    private static class TheLocalFix implements LocalQuickFix {
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
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement();
            if (target instanceof BinaryExpression) {
                final BinaryExpression expression = (BinaryExpression) target;
                final PsiElement left             = expression.getLeftOperand();
                final PsiElement right            = expression.getRightOperand();
                if (null == left || null == right) {
                    return;
                }

                final PsiElement leftCopy = left.copy();
                left.replace(right);
                right.replace(leftCopy);
            }
        }
    }
}
