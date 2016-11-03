package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class LoopWhichDoesNotLoopInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This loop does not loop";

    @NotNull
    public String getShortName() {
        return "LoopWhichDoesNotLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                this.inspectBody(foreach);
            }
            public void visitPhpFor(For forStatement) {
                this.inspectBody(forStatement);
            }
            public void visitPhpWhile(While whileStatement) {
                this.inspectBody(whileStatement);
            }

            private void inspectBody(PhpPsiElement loop) {
                final GroupStatement groupStatement = ExpressionSemanticUtil.getGroupStatement(loop);
                if (null == groupStatement) {
                    return;
                }

                final PsiElement lastExpression = ExpressionSemanticUtil.getLastStatement(groupStatement);
                boolean isLoopTerminatedWithLastExpression = (
                    lastExpression instanceof PhpBreak ||
                    lastExpression instanceof PhpReturn ||
                    lastExpression instanceof PhpThrow
                );
                /* loop is empty or terminates on first iteration */
                if (null != lastExpression && !isLoopTerminatedWithLastExpression) {
                    return;
                }

                /* prevent false-positives when loop has if { ...; continue; } at some place */
                for (PsiElement expressionInLoopBody : groupStatement.getStatements()) {
                    if (!(expressionInLoopBody instanceof If)) {
                        continue;
                    }

                    final GroupStatement previous = ExpressionSemanticUtil.getGroupStatement(expressionInLoopBody);
                    if (null != previous) {
                        final PsiElement terminationCandidate = ExpressionSemanticUtil.getLastStatement(previous);
                        if (terminationCandidate instanceof PhpContinue || terminationCandidate instanceof PhpReturn) {
                            return;
                        }
                    }
                }

                holder.registerProblem(loop.getFirstChild(), strProblemDescription, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
