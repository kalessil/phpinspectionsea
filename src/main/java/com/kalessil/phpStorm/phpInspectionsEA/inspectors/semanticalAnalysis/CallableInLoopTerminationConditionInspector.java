package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CallableInLoopTerminationConditionInspector extends BasePhpInspection {
    private static final String message = "Avoid callables in loop conditionals for better performance.";

    @NotNull
    public String getShortName() {
        return "CallableInLoopTerminationConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFor(final For forStatement) {
                /* TODO: re-evaluate searching in tree for catching more cases */
                final PhpPsiElement[] conditions = forStatement.getConditionalExpressions();

                if ((conditions.length != 1) ||
                    !(conditions[0] instanceof BinaryExpression)) {
                    return;
                }

                final BinaryExpression condition = (BinaryExpression) conditions[0];

                if (OpenapiTypesUtil.isFunctionReference(condition.getRightOperand()) ||
                    OpenapiTypesUtil.isFunctionReference(condition.getLeftOperand())) {
                    problemsHolder.registerProblem(condition, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}
