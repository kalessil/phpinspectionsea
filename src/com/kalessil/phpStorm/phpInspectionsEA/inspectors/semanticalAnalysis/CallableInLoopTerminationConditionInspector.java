package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class CallableInLoopTerminationConditionInspector extends BasePhpInspection {
    private static final String strProblemPattern = "'for (%existingInit%%newInit%; %newCheck%; ...)' should be used for better performance";
    private static final String strProblemDescription = "Callable result should be stored outside of the loop for better performance";

    @NotNull
    public String getShortName() {
        return "CallableInLoopTerminationConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @NotNull
            private String generateMessage(For expression, BinaryExpression problematicExpression) {
                /* extract part of expression we need to report */
                PsiElement referenceCandidate = problematicExpression.getRightOperand();
                PsiElement variableCandidate = problematicExpression.getLeftOperand();
                boolean leftToRight = true;
                if (!(referenceCandidate instanceof FunctionReference)) {
                    referenceCandidate = problematicExpression.getLeftOperand();
                    variableCandidate = problematicExpression.getRightOperand();
                    leftToRight = false;
                }

                /* check if we can customize message at all */
                PsiElement operation = problematicExpression.getOperation();
                if (null == operation || null == variableCandidate || null == referenceCandidate) {
                    return strProblemDescription;
                }

                /* try extracting variable name from variable candidate */
                String variableName = null;
                if (variableCandidate instanceof Variable) {
                    variableName = ((Variable) variableCandidate).getName();
                }
                variableName = StringUtil.isEmpty(variableName) ? "loopsCount" : variableName + "Max";

                /* generate message */
                final boolean hasInit = expression.getInitialExpressions().length > 0;
                return strProblemPattern
                        .replace("%existingInit%", hasInit ? "..., " : "")
                        .replace("%newInit%", "$" + variableName + " = " + referenceCandidate.getText())
                        .replace("%newCheck%",
                            leftToRight
                                ? variableCandidate.getText() + " " + operation.getText() + " " + "$" + variableName
                                : "$" + variableName + " " + operation.getText() + " " + variableCandidate.getText()
                        );
            }

            public void visitPhpFor(For expression) {
                PhpPsiElement[] arrConditions = expression.getConditionalExpressions();
                if (arrConditions.length != 1 || !(arrConditions[0] instanceof BinaryExpression)) {
                    return;
                }

                BinaryExpression objCondition = (BinaryExpression) arrConditions[0];

                PsiElement objRightOperand = objCondition.getRightOperand();
                PsiElement objLeftOperand = objCondition.getLeftOperand();
                if (
                    (objRightOperand instanceof FunctionReference && !(objRightOperand instanceof MethodReference)) ||
                    (objLeftOperand instanceof FunctionReference  && !(objLeftOperand instanceof MethodReference))
                ) {
                    String message = generateMessage(expression, objCondition);
                    holder.registerProblem(objCondition, message, ProblemHighlightType.GENERIC_ERROR);
                }
            }
        };
    }
}