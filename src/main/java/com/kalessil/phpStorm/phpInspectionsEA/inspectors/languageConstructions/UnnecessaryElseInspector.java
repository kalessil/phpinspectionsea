package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UnnecessaryElseFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
public class UnnecessaryElseInspector extends BasePhpInspection {

    public boolean CHECK_ELSEIF = false;


    private static final String message = "Keyword else can be safely removed.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryElseInspection";
    }


    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpElse(Else elseStatement) {
                super.visitPhpElse(elseStatement);
                boolean isElseIf = (elseStatement.getLastChild() instanceof If);
                if (!isElseIf || CHECK_ELSEIF) {
                    visitElseKeywords(elseStatement);
                }
            }

            @Override
            public void visitPhpElseIf(ElseIf elseIfStatement) {
                super.visitPhpElseIf(elseIfStatement);
                if (CHECK_ELSEIF) {
                    visitElseKeywords(elseIfStatement);
                }
            }


            private void visitElseKeywords(Statement elseStatement) {
                GroupStatement group = ExpressionSemanticUtil.getGroupStatement(elseStatement.getParent());

                if (null == group || !hasBraces(group)) {
                    return;
                }

                PsiElement[] childrenStatements = group.getChildren();
                int childLen = childrenStatements.length;
                if (childLen == 0) {
                    return;
                }

                int key = childLen - 1;
                PsiElement lastChildElement = childrenStatements[key];
                if (lastChildElement == null) {
                    return;
                }

                boolean isExitStatement = (lastChildElement instanceof Statement && lastChildElement.getFirstChild() instanceof PhpExit);
                if (isExitStatement || lastChildElement instanceof PhpReturn || lastChildElement instanceof PhpThrow || lastChildElement instanceof PhpContinue || lastChildElement instanceof PhpBreak) {
                    holder.registerProblem(elseStatement.getFirstChild(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, (LocalQuickFix) new UnnecessaryElseFixer());
                }


            }
        };
    }

    private boolean hasBraces(GroupStatement groupStatement) {
        PsiElement lastElement = groupStatement.getLastChild();
        return (lastElement != null && (lastElement.getNode().getElementType() == PhpTokenTypes.chRBRACE));
    }


    public JComponent createOptionsPanel() {
        return new OptionsPanel().getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox checkElseIf;


        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            checkElseIf = new JCheckBox("Check for elseif statement", CHECK_ELSEIF);

            checkElseIf.addChangeListener(e -> {
                CHECK_ELSEIF = checkElseIf.isSelected();
            });
            optionsPanel.add(checkElseIf, "wrap");

        }

        JPanel getComponent() {
            return optionsPanel;
        }
    }
}

