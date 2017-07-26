package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class MissingOrEmptyGroupStatementInspector extends BasePhpInspection {
    private static final String messageMissingBrackets = "Wrap constructs' body within a block.";
    private static final String messageEmptyBody       = "Empty block.";

    @NotNull
    public String getShortName() {
        return "MissingOrEmptyGroupStatementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpIf(If ifStatement) {
                this.checkBrackets(ifStatement);
            }

            public void visitPhpElseIf(ElseIf elseIfStatement) {
                this.checkBrackets(elseIfStatement);
            }

            public void visitPhpElse(Else elseStatement) {
                this.checkBrackets(elseStatement);
            }

            public void visitPhpForeach(ForeachStatement foreach) {
                this.checkBrackets(foreach);
            }

            public void visitPhpFor(For forStatement) {
                this.checkBrackets(forStatement);
            }

            public void visitPhpWhile(While whileStatement) {
                this.checkBrackets(whileStatement);
            }

            public void visitPhpDoWhile(DoWhile doWhileStatement) {
                this.checkBrackets(doWhileStatement);
            }

            private void checkBrackets(@NotNull PhpPsiElement construct) {
                final PsiElement target   = construct.getFirstChild();
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(construct);
                if (null != body) {
                    if (0 == ExpressionSemanticUtil.countExpressionsInGroup(body)) {
                        holder.registerProblem(target, messageEmptyBody, ProblemHighlightType.WEAK_WARNING);
                    }
                    return;
                }

                /* community feedback: do not report "else if" constructions */
                if (construct instanceof Else && construct.getFirstPsiChild() instanceof If) {
                    return;
                }

                holder.registerProblem(target, messageMissingBrackets, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}
