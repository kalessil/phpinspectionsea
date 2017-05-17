package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
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
    private static final String messagePattern = "'for (%s%s; %s; ...)' should be used for better performance.";

    @NotNull
    public String getShortName() {
        return "CallableInLoopTerminationConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            private void checkStatement(@NotNull final For expression, @NotNull final BinaryExpression problematicExpression) {
                /* extract part of expression we need to report */
                PsiElement referenceCandidate = problematicExpression.getRightOperand();
                PsiElement variableCandidate  = problematicExpression.getLeftOperand();
                boolean    leftToRight        = true;

                if (!(referenceCandidate instanceof FunctionReference)) {
                    referenceCandidate = problematicExpression.getLeftOperand();
                    variableCandidate = problematicExpression.getRightOperand();
                    leftToRight = false;
                }

                /* check if we can customize message at all */
                final PsiElement operation = problematicExpression.getOperation();

                assert operation != null;
                assert variableCandidate != null;
                assert referenceCandidate != null;

                /* try extracting variable name from variable candidate */
                String variableName = null;

                if (variableCandidate instanceof Variable) {
                    variableName = ((Variable) variableCandidate).getName();
                }

                variableName = StringUtil.isEmpty(variableName) ? "loopsCount" : (variableName + "Max");

                /* generate message */
                final boolean hasInit = expression.getInitialExpressions().length > 0;
                final String newCheck = leftToRight
                                        ? (variableCandidate.getText() + ' ' + operation.getText() + ' ' + '$' + variableName)
                                        : ('$' + variableName + ' ' + operation.getText() + ' ' + variableCandidate.getText());

                final String message = String.format(messagePattern,
                                                     hasInit ? "..., " : "",
                                                     '$' + variableName + " = " + referenceCandidate.getText(),
                                                     newCheck);

                problemsHolder.registerProblem(problematicExpression, message, ProblemHighlightType.GENERIC_ERROR);
            }

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
                    checkStatement(forStatement, condition);
                }
            }
        };
    }
}
