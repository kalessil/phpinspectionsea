package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LoopWhichDoesNotLoopInspector extends BasePhpInspection {
    private static final String message = "This loop does not loop.";

    @NotNull
    public String getShortName() {
        return "LoopWhichDoesNotLoopInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                this.inspectBody(foreach);
            }
            public void visitPhpFor(@NotNull For forStatement) {
                this.inspectBody(forStatement);
            }
            public void visitPhpWhile(@NotNull While whileStatement) {
                this.inspectBody(whileStatement);
            }
            public void visitPhpDoWhile(@NotNull DoWhile doWhileStatement) {
                this.inspectBody(doWhileStatement);
            }

            private void inspectBody(@NotNull PhpPsiElement loop) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (null == body) {
                    return;
                }

                final PsiElement lastExpression                  = ExpressionSemanticUtil.getLastStatement(body);
                final boolean isLoopTerminatedWithLastExpression = lastExpression instanceof PhpBreak ||
                                                                   lastExpression instanceof PhpReturn ||
                                                                   lastExpression instanceof PhpThrow;
                /* loop is empty or terminates on first iteration */
                if (null != lastExpression && !isLoopTerminatedWithLastExpression) {
                    return;
                }

                /* detect continue statements, which makes the loop looping */
                final Collection<PhpContinue> continues = PsiTreeUtil.findChildrenOfType(body, PhpContinue.class);
                for (final PhpContinue expression : continues) {
                    int nestingLevel  = 0;
                    PsiElement parent = expression.getParent();
                    while (null != parent && !(parent instanceof Function) && !(parent instanceof PsiFile)) {
                        if (OpenapiTypesUtil.isLoop(parent)) {
                            ++nestingLevel;

                            if (parent == loop) {
                                /* extract level of continuation from the statement */
                                int continueLevel = 1;
                                if (null != expression.getArgument()) {
                                    try {
                                        continueLevel = Integer.parseInt(expression.getArgument().getText());
                                    } catch (NumberFormatException notParsed) {
                                        continueLevel = 1;
                                    }
                                }

                                /* matched continue for the current loop */
                                if (continueLevel == nestingLevel) {
                                    continues.clear();
                                    return;
                                }
                            }
                        }
                        parent = parent.getParent();
                    }
                }
                continues.clear();

                holder.registerProblem(loop.getFirstChild(), message);
            }
        };
    }
}
