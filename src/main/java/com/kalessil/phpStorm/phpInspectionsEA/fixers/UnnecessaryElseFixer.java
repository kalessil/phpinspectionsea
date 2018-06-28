package com.kalessil.phpStorm.phpInspectionsEA.fixers;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Else;
import com.jetbrains.php.lang.psi.elements.ElseIf;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.If;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

final public class UnnecessaryElseFixer implements LocalQuickFix {
    private static final String title = "Split the workflows";

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
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element    = descriptor.getPsiElement();
        final PsiElement expression = null == element ? null : element.getParent();
        final PsiElement newline    = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, "\n");
        if (null == expression || null == newline || project.isDisposed()) {
            return;
        }

        if (expression instanceof Else) {
            final Else elseStatement        = (Else) expression;
            final If parentIfExpression     = (If) expression.getParent();
            final PsiElement parentIfHolder = parentIfExpression.getParent();

            if (elseStatement.getFirstPsiChild() instanceof If) { /* handle 'else if' */
                final If nestedIfCopy = (If) elseStatement.getFirstPsiChild().copy();
                elseStatement.delete();

                parentIfHolder.addAfter(nestedIfCopy, parentIfExpression);
                parentIfHolder.addAfter(newline, parentIfExpression);

                return;
            }

            if (elseStatement.getFirstPsiChild() instanceof GroupStatement) { /* handle 'else {} ' */
                final GroupStatement elseBodyCopy = (GroupStatement) elseStatement.getFirstPsiChild().copy();
                elseStatement.delete();

                final PsiElement startBracket = elseBodyCopy.getFirstChild();
                final PsiElement endBracket   = elseBodyCopy.getLastChild();
                PsiElement last               = endBracket.getPrevSibling();
                if (last instanceof PsiWhiteSpace) {
                    last = last.getPrevSibling();
                }
                while (null != last) {
                    if (last != endBracket && last != startBracket) {
                        parentIfHolder.addAfter(last, parentIfExpression);
                    }
                    last = last.getPrevSibling();
                }

                /* trailing spacing should not be influence by else body */
                final PsiElement trailingSpaceCandidate = parentIfExpression.getNextSibling();
                if (trailingSpaceCandidate instanceof PsiWhiteSpace) {
                    trailingSpaceCandidate.replace(newline);
                } else {
                    parentIfHolder.addAfter(newline, parentIfExpression);
                }

                return;
            }
        }

        if (expression instanceof ElseIf) { /* handle 'elseif': messed up to not loose cursor after fixing... */
            final ElseIf elseIfStatement = (ElseIf) expression;
            final If parentIfExpression  = (If) expression.getParent();

            /* back up original if */
            final If newIf = PhpPsiElementFactory.createPhpPsiFromText(project, If.class, "if (true) {\n}");
            newIf.getCondition().replace(parentIfExpression.getCondition());
            ExpressionSemanticUtil.getGroupStatement(newIf).replace(ExpressionSemanticUtil.getGroupStatement(parentIfExpression));

            /* drop the elseif, backup resulted construct */
            parentIfExpression.getCondition().replace(elseIfStatement.getCondition().copy());
            ExpressionSemanticUtil.getGroupStatement(parentIfExpression).replace(ExpressionSemanticUtil.getGroupStatement(elseIfStatement).copy());
            elseIfStatement.delete();
            final PsiElement followUpIf = parentIfExpression.copy();

            /* insert following up if, which was backed up */
            final PsiElement parentIfHolder = parentIfExpression.getParent();
            parentIfHolder.addAfter(followUpIf, parentIfExpression);
            parentIfHolder.addAfter(newline, parentIfExpression);

            /* actualize if-statement with clean one */
            parentIfHolder.addBefore(newIf, parentIfExpression);
            parentIfExpression.delete();
        }
    }
}
