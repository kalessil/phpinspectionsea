package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Catch;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Try;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.apache.commons.lang3.StringUtils;
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
    private static final String messagePattern          = "It is possible that some of the statements contained in the try block can be extracted into their own methods or functions (we recommend that you do not include more than three statements per try block).";
    private static final String messageFailSilently     = "The exception being ignored, please don't fail silently and at least log it.";
    private static final String messageChainedException = "The exception being ignored, please log it or use chained exceptions.";

    @NotNull
    @Override
    public String getShortName() {
        return "BadExceptionsProcessingInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Badly organized exception handling";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpTry(@NotNull Try tryStatement) {
                final GroupStatement body  = ExpressionSemanticUtil.getGroupStatement(tryStatement);
                final int expressionsCount = body == null ? 0 : ExpressionSemanticUtil.countExpressionsInGroup(body);
                if (expressionsCount > 3) {
                    holder.registerProblem(
                            tryStatement.getFirstChild(),
                            MessagesPresentationUtil.prefixWithEa(messagePattern.replace("%c%", String.valueOf(expressionsCount)))
                    );
                }
            }

            @Override
            public void visitPhpCatch(@NotNull Catch catchStatement) {
                final Variable variable   = catchStatement.getException();
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(catchStatement);
                if (variable != null && body != null) {
                    final String variableName = variable.getName();
                    if (!StringUtils.isEmpty(variableName)) { /* incomplete catch statement */
                        boolean isVariableUsed = false;
                        for (final Variable usedVariable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                            if (usedVariable.getName().equals(variableName)) {
                                isVariableUsed = true;
                                break;
                            }
                        }
                        if (!isVariableUsed) {
                            if (ExpressionSemanticUtil.countExpressionsInGroup(body) == 0) {
                                holder.registerProblem(
                                        variable,
                                        MessagesPresentationUtil.prefixWithEa(messageFailSilently)
                                );
                            } else {
                                holder.registerProblem(
                                        variable,
                                        MessagesPresentationUtil.prefixWithEa(messageChainedException)
                                );
                            }
                        }
                    }
                }
            }
        };
    }
}
