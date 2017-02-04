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
import net.miginfocom.swing.MigLayout;
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
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean CONFIGURED           = false;
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
                /* verify general structure */
                final IElementType operator = CONFIGURED ? expression.getOperationType() : null;
                final PsiElement left       = CONFIGURED ? expression.getLeftOperand() : null;
                final PsiElement right      = CONFIGURED ? expression.getRightOperand() : null;
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
        return (new ComparisonOperandsOrderInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox preferYodaStyle;
        final private JCheckBox preferRegularStyle;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            preferYodaStyle    = new JCheckBox("Prefer yoda style", PREFER_YODA_STYLE);
            preferRegularStyle = new JCheckBox("Prefer regular style", PREFER_REGULAR_STYLE);

            preferYodaStyle.addChangeListener(e -> {
                PREFER_YODA_STYLE = preferYodaStyle.isSelected();
                if (PREFER_YODA_STYLE) {
                    preferRegularStyle.setSelected(false);
                    PREFER_REGULAR_STYLE = false;
                }

                CONFIGURED = PREFER_YODA_STYLE || PREFER_REGULAR_STYLE;
            });
            optionsPanel.add(preferYodaStyle, "wrap");

            preferRegularStyle.addChangeListener(e -> {
                PREFER_REGULAR_STYLE = preferRegularStyle.isSelected();
                if (PREFER_REGULAR_STYLE) {
                    preferYodaStyle.setSelected(false);
                    PREFER_YODA_STYLE = false;
                }

                CONFIGURED = PREFER_YODA_STYLE || PREFER_REGULAR_STYLE;
            });
            optionsPanel.add(preferRegularStyle, "wrap");
        }

        JPanel getComponent() {
            return optionsPanel;
        }
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
