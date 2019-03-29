package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class BadExceptionsProcessingInspector extends BasePhpInspection {
    private static final String messagePattern          = "Consider moving non-related statements (%s in total) outside the try-block or refactoring the try-body into a function/method.";
    private static final String messageFailSilently     = "The exception being ignored, please don't fail silently and at least log it.";
    private static final String messageChainedException = "The exception being ignored, please log it or use chained exceptions.";
    private static final String messageRethrown         = "The exception being re-throws without any processing.";

    @NotNull
    public String getShortName() {
        return "BadExceptionsProcessingInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTry(@NotNull Try tryStatement) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(tryStatement))           { return; }

                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(tryStatement);
                if (body != null) {
                    final int expressionsCount = ExpressionSemanticUtil.countExpressionsInGroup(body);
                    if (expressionsCount > 3) {
                        holder.registerProblem(
                                tryStatement.getFirstChild(),
                                String.format(messagePattern, String.valueOf(expressionsCount))
                        );
                    }
                }
            }

            @Override
            public void visitPhpCatch(@NotNull Catch catchStatement) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(catchStatement))         { return; }

                final Variable variable = catchStatement.getException();
                if (variable != null) {
                    final String variableName = variable.getName();
                    if (!variableName.isEmpty()) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(catchStatement);
                        if (body != null) {
                            boolean isVariableUsed = PsiTreeUtil.findChildrenOfType(body, Variable.class).stream()
                                    .anyMatch(v -> variableName.equals(v.getName()));
                            if (!isVariableUsed) {
                                if (ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                                    holder.registerProblem(variable, messageFailSilently);
                                } else {
                                    holder.registerProblem(variable, messageChainedException);
                                }
                            } else {
                                final PsiElement last = ExpressionSemanticUtil.getLastStatement(body);
                                if (last instanceof PhpThrow) {
                                    final PhpThrow lastThrow  = (PhpThrow) last;
                                    final PsiElement argument = lastThrow.getArgument();
                                    if (argument != null && OpenapiEquivalenceUtil.areEqual(argument, variable)) {
                                        holder.registerProblem(variable, messageRethrown);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
