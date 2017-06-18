package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

final public class ExpressionCostEstimateUtil {
    public final static Set<String> predefinedVars = new HashSet<>();
    static {
        predefinedVars.add("_GET");
        predefinedVars.add("_POST");
        predefinedVars.add("_SESSION");
        predefinedVars.add("_REQUEST");
        predefinedVars.add("_FILES");
        predefinedVars.add("_COOKIE");
        predefinedVars.add("_ENV");
        predefinedVars.add("_SERVER");
        predefinedVars.add("GLOBALS");
        predefinedVars.add("HTTP_RAW_POST_DATA");
    }

    /**
     * Estimates execution cost on basis 0-10 for simple parts. Complex constructions can be estimated
     * to more than 10.
     *
     * @param objExpression to estimate for execution cost
     * @return costs
     */
    public static int getExpressionCost(@Nullable PsiElement objExpression, @NotNull Set<String> functionsSetToAllow) {
        objExpression = ExpressionSemanticUtil.getExpressionTroughParenthesis(objExpression);

        if (
            null == objExpression ||
            objExpression instanceof ConstantReference ||
            objExpression instanceof StringLiteralExpression ||
            objExpression instanceof ClassReference ||
            objExpression instanceof Variable
        ) {
            return 0;
        }

        /* additional factor is due to hash-maps internals not considered */
        if (objExpression instanceof ClassConstantReference) {
            return 0;
        }
        if (objExpression instanceof FieldReference) {
            /* $x->y and $x->y->z to have the same cost. Because of magic methods, which are slower. */
            return getExpressionCost(((FieldReference) objExpression).getFirstPsiChild(), functionsSetToAllow);
        }

        /* additional factor is due to hash-maps internals */
        if (objExpression instanceof ArrayAccessExpression) {
            final ArrayAccessExpression arrayAccess = (ArrayAccessExpression) objExpression;
            final ArrayIndex arrayIndex             =  arrayAccess.getIndex();

            int intOwnCosts = getExpressionCost(arrayAccess.getValue(), functionsSetToAllow);
            if (null != arrayIndex) {
                intOwnCosts += getExpressionCost(arrayIndex.getValue(), functionsSetToAllow);
            }

            /* default additional cost */
            int additionalCosts = 1;

            /* for pre-defined variables add no costs:
                @see https://bitbucket.org/kalessil/phpinspectionsea/issues/239/non-optimal-if-conditions-incorrect */
            if (arrayAccess.getValue() instanceof Variable) {
                final String variableName = arrayAccess.getValue().getName();
                if (
                    !StringUtil.isEmpty(variableName) && variableName.charAt(0) == '_' &&
                    predefinedVars.contains(variableName)
                ) {
                    additionalCosts = 0;
                }
            }

            return (additionalCosts + intOwnCosts);
        }

        /* empty counts too much as empty, so it still sensitive overhead, but not add any factor */
        if (objExpression instanceof PhpEmpty) {
            int intArgumentsCost = 0;
            for (PsiElement objParameter : ((PhpEmpty) objExpression).getVariables()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            return intArgumentsCost;
        }

        /* isset brings no additional costs, often used for aggressive optimization */
        if (objExpression instanceof PhpIsset) {
            int intArgumentsCost = 0;
            for (PsiElement objParameter : ((PhpIsset) objExpression).getVariables()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            return intArgumentsCost;
        }

        /* didn't see anu usages in if, but who knows */
        if (objExpression instanceof PhpUnset) {
            int intArgumentsCost = 0;
            for (PsiElement objParameter : ((PhpUnset) objExpression).getArguments()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            return intArgumentsCost;
        }

        if (objExpression instanceof FunctionReference) {
            int intArgumentsCost = 0;
            for (PsiElement objParameter : ((FunctionReference) objExpression).getParameters()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            /* quite complex part - differentiate methods, functions and specially type-check functions */
            if (objExpression instanceof MethodReference) {
                intArgumentsCost += getExpressionCost(((MethodReference) objExpression).getFirstPsiChild(), functionsSetToAllow);
                intArgumentsCost += 5;
            } else {
                final String strFunctionName = ((FunctionReference) objExpression).getName();
                /* type-check functions and rest functions */
                if (!StringUtil.isEmpty(strFunctionName) && functionsSetToAllow.contains(strFunctionName)) {
                    intArgumentsCost += 0;
                } else {
                    intArgumentsCost += 5;
                }
            }

            return intArgumentsCost;
        }

        if (objExpression instanceof UnaryExpression) {
            return getExpressionCost(((UnaryExpression) objExpression).getValue(), functionsSetToAllow);
        }

/*        if (objExpression instanceof TernaryExpression) {
            return
                this.getExpressionCost(((TernaryExpression) objExpression).getCondition()) +
                Math.max(
                        this.getExpressionCost(((TernaryExpression) objExpression).getTrueVariant()),
                        this.getExpressionCost(((TernaryExpression) objExpression).getFalseVariant())
                );
        }
*/

        if (objExpression instanceof BinaryExpression) {
            return
                getExpressionCost(((BinaryExpression) objExpression).getRightOperand(), functionsSetToAllow) +
                getExpressionCost(((BinaryExpression) objExpression).getLeftOperand(), functionsSetToAllow);
        }

        if (objExpression instanceof ArrayCreationExpression) {
            int intCosts = 0;
            for (ArrayHashElement objEntry : ((ArrayCreationExpression) objExpression).getHashElements()) {
                intCosts += getExpressionCost(objEntry.getKey(), functionsSetToAllow);
                intCosts += getExpressionCost(objEntry.getValue(), functionsSetToAllow);
            }
            return intCosts;
        }

        if (
            objExpression instanceof PhpExpression &&
            objExpression.getNode().getElementType() == PhpElementTypes.NUMBER
        ) {
            return 0;
        }

        if (objExpression instanceof AssignmentExpression) {
            return getExpressionCost(((AssignmentExpression) objExpression).getValue(), functionsSetToAllow);
        }

        return 10;
    }
}