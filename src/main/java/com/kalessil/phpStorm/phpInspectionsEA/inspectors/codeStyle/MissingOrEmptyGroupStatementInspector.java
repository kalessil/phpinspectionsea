package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
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

public class MissingOrEmptyGroupStatementInspector extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_EMPTY_BODY = true;

    private static final String messageMissingBrackets = "Wrap constructs' body within a block (PSR-2 recommendations).";
    private static final String messageEmptyBody       = "Statement has empty block.";

    @NotNull
    public String getShortName() {
        return "MissingOrEmptyGroupStatementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If ifStatement) {
                this.checkBrackets(ifStatement);
            }
            @Override
            public void visitPhpElseIf(@NotNull ElseIf elseIfStatement) {
                this.checkBrackets(elseIfStatement);
            }
            @Override
            public void visitPhpElse(@NotNull Else elseStatement) {
                this.checkBrackets(elseStatement);
            }
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreachStatement) {
                this.checkBrackets(foreachStatement);
            }
            @Override
            public void visitPhpFor(@NotNull For forStatement) {
                this.checkBrackets(forStatement);
            }
            @Override
            public void visitPhpWhile(@NotNull While whileStatement) {
                this.checkBrackets(whileStatement);
            }
            @Override
            public void visitPhpDoWhile(@NotNull DoWhile doWhileStatement) {
                this.checkBrackets(doWhileStatement);
            }

            private void checkBrackets(@NotNull PhpPsiElement construct) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(construct);
                if (body != null) {
                    if (REPORT_EMPTY_BODY && ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                        holder.registerProblem(construct.getFirstChild(), messageEmptyBody);
                    }
                    return;
                }
                /* community feedback: do not report "else if" constructions */
                else if (construct instanceof Else && construct.getLastChild() instanceof If) {
                    return;
                }

                holder.registerProblem(construct.getFirstChild(), messageMissingBrackets, new WrapBodyFix());
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Report empty group statements", REPORT_EMPTY_BODY, (isSelected) -> REPORT_EMPTY_BODY = isSelected)
        );
    }

    private static final class WrapBodyFix implements LocalQuickFix {
        private static final String title = "Add the group statement";

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
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            PsiElement target = problemDescriptor.getPsiElement().getParent();
            if (target instanceof ControlStatement) {
                target = ((ControlStatement) target).getStatement();
            } else if (target instanceof Else) {
                target = ((Else) target).getStatement();
            }

            if (target != null && !project.isDisposed()) {
                final String code        = String.format("if() { %s }", target.getText());
                final If donor           = PhpPsiElementFactory.createPhpPsiFromText(project, If.class, code);
                final PsiElement implant = donor.getStatement();
                if (implant != null) {
                    target.replace(implant);
                }
            }
        }
    }
}
