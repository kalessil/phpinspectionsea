package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UnnecessaryContinueInspector extends BasePhpInspection {
    private static final String message = "It's not really makes sense placing continue here as loop will continue from here anyway.";

    @NotNull
    public String getShortName() {
        return "UnnecessaryContinueInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpContinue(@NotNull PhpContinue continueStatement) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }

                if (continueStatement.getArgument() == null) {
                    final PsiElement continuedStatement = this.getContinuedStatement(continueStatement);
                    if (continuedStatement != null) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(continuedStatement);
                        if (body != null) {
                            final List<PsiElement> lastStatements = this.collectLastStatements(body, new ArrayList<>());
                            if (lastStatements.stream().anyMatch(statement -> continueStatement == statement)) {
                                holder.registerProblem(continueStatement, message, new RemoveUnnecessaryStatementFix());
                            }
                            lastStatements.clear();
                        }
                    }
                }
            }

            @NotNull
            private  List<PsiElement> collectLastStatements(@NotNull GroupStatement body, @NotNull List<PsiElement> lastStatements) {
                final PsiElement lastStatement = ExpressionSemanticUtil.getLastStatement(body);
                if (lastStatement instanceof PhpContinue) {
                    lastStatements.add(lastStatement);
                } else if (lastStatement instanceof If) {
                    /* collect branches */
                    final If ifStatement            = (If) lastStatement;
                    final List<PsiElement> branches = new ArrayList<>();
                    branches.addAll(Arrays.asList(ifStatement, ifStatement.getElseBranch()));
                    branches.addAll(Arrays.asList(ifStatement.getElseIfBranches()));
                    /* iterate bodies, keep collecting statements */
                    branches.stream()
                            .filter(Objects::nonNull).map(ExpressionSemanticUtil::getGroupStatement)
                            .filter(Objects::nonNull).forEach(block -> collectLastStatements(block, lastStatements));
                    branches.clear();
                }
                return lastStatements;
            }

            @Nullable
            private PsiElement getContinuedStatement(@NotNull PhpContinue expression) {
                PsiElement result  = null;
                PsiElement current = expression.getParent();
                while (current != null && !(current instanceof Function) && !(current instanceof PsiFile)) {
                    if (current instanceof PhpSwitch) {
                        break;
                    }
                    if (OpenapiTypesUtil.isLoop(current)) {
                        result = current;
                        break;
                    }
                    current = current.getParent();
                }
                return result;
            }
        };
    }

    private static final class RemoveUnnecessaryStatementFix implements LocalQuickFix {
        private static final String title = "Remove unnecessary statement";

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
            final PsiElement expression = descriptor.getPsiElement();
            if (expression != null && !project.isDisposed()) {
                final PsiElement previous = expression.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous.delete();
                }
                expression.delete();
            }
        }
    }

}
