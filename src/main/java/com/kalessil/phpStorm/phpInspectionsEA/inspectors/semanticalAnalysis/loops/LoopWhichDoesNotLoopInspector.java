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
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement loop) {
                if (!this.isLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpFor(@NotNull For loop) {
                if (!this.isLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpWhile(@NotNull While loop) {
                if (!this.isLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            @Override
            public void visitPhpDoWhile(@NotNull DoWhile loop) {
                if (!this.isLooping(loop)) {
                    holder.registerProblem(loop.getFirstChild(), message);
                }
            }

            private boolean isLooping(@NotNull PhpPsiElement loop) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(loop);
                if (null == body) {
                    return true;
                }

                final PsiElement lastExpression                  = ExpressionSemanticUtil.getLastStatement(body);
                final boolean isLoopTerminatedWithLastExpression = lastExpression instanceof PhpBreak ||
                                                                   lastExpression instanceof PhpReturn ||
                                                                   lastExpression instanceof PhpThrow;
                /* loop is empty or terminates on first iteration */
                if (null != lastExpression && !isLoopTerminatedWithLastExpression) {
                    return true;
                }

                /* detect continue statements, which makes the loop looping */
                for (final PhpContinue expression : PsiTreeUtil.findChildrenOfType(body, PhpContinue.class)) {
                    int nestingLevel  = 0;
                    PsiElement parent = expression.getParent();
                    while (null != parent && !(parent instanceof Function) && !(parent instanceof PsiFile)) {
                        if (OpenapiTypesUtil.isLoop(parent)) {
                            ++nestingLevel;

                            if (parent == loop) {
                                /* extract level of continuation from the statement */
                                int continueLevel         = 1;
                                final PsiElement argument = expression.getArgument();
                                if (null != argument) {
                                    try {
                                        continueLevel = Integer.parseInt(argument.getText());
                                    } catch (final NumberFormatException notParsed) {
                                        continueLevel = 1;
                                    }
                                }

                                /* matched continue for the current loop */
                                if (continueLevel == nestingLevel) {
                                    return true;
                                }
                            }
                        }
                        parent = parent.getParent();
                    }
                }

                return false;
            }
        };
    }
}
