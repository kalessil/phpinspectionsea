package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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
                final PsiElement target   = construct.getFirstChild();
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(construct);
                if (body != null) {
                    if (REPORT_EMPTY_BODY && ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                        holder.registerProblem(target, messageEmptyBody);
                    }
                    return;
                }
                /* community feedback: do not report "else if" constructions */
                else if (construct instanceof Else && construct.getFirstPsiChild() instanceof If) {
                    return;
                }

                holder.registerProblem(target, messageMissingBrackets);
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Report empty group statements", REPORT_EMPTY_BODY, (isSelected) -> REPORT_EMPTY_BODY = isSelected)
        );
    }
}
