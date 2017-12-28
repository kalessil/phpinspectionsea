package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
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
    private static final String message = "...";

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
                                holder.registerProblem(continueStatement, message);
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
                    /* collect bodies */
                    final If ifStatement              = (If) lastStatement;
                    final List<GroupStatement> bodies = new ArrayList<>();
                    bodies.add(ExpressionSemanticUtil.getGroupStatement(ifStatement));
                    Arrays.stream(ifStatement.getElseIfBranches())
                            .forEach(elseIf -> bodies.add(ExpressionSemanticUtil.getGroupStatement(elseIf)));
                    final Else elseStatement = ifStatement.getElseBranch();
                    bodies.add(elseStatement == null ? null : ExpressionSemanticUtil.getGroupStatement(elseStatement));
                    /* iterate bodies, keep collecting statements */
                    bodies.stream().filter(Objects::nonNull).forEach(block -> collectLastStatements(block, lastStatements));
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
}
