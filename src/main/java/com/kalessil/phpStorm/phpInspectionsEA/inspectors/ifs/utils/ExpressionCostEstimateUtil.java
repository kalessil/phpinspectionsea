package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

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
            objExpression == null ||
            objExpression instanceof ConstantReference ||
            objExpression instanceof ClassReference ||
            objExpression instanceof ClassConstantReference ||
            OpenapiTypesUtil.isNumber(objExpression)
        ) {
            return 0;
        }

        if (
            objExpression instanceof Variable ||
            objExpression instanceof StringLiteralExpression ||
            objExpression instanceof FieldReference
        ) {
            /* It can be nested expression in there, incl. injections and etc */
            return Stream.of(objExpression.getChildren())
                    .mapToInt(c -> getExpressionCost(c, functionsSetToAllow))
                    .sum();
        }

        /* hash-maps is well optimized, hence no additional costs */
        if (objExpression instanceof ArrayAccessExpression) {
            final ArrayAccessExpression arrayAccess = (ArrayAccessExpression) objExpression;
            final ArrayIndex arrayIndex             =  arrayAccess.getIndex();

            int intOwnCosts = getExpressionCost(arrayAccess.getValue(), functionsSetToAllow);
            if (null != arrayIndex) {
                intOwnCosts += getExpressionCost(arrayIndex.getValue(), functionsSetToAllow);
            }

            return intOwnCosts;
        }

        /* empty counts too much as empty, so it still sensitive overhead, but not add any factor */
        if (objExpression instanceof PhpEmpty) {
            int intArgumentsCost = 0;
            for (final PsiElement objParameter : ((PhpEmpty) objExpression).getVariables()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            return intArgumentsCost;
        }

        /* isset brings no additional costs, often used for aggressive optimization */
        if (objExpression instanceof PhpIsset) {
            int intArgumentsCost = 0;
            for (final PsiElement objParameter : ((PhpIsset) objExpression).getVariables()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            return intArgumentsCost;
        }

        if (objExpression instanceof FunctionReference) {
            int intArgumentsCost = 0;
            for (final PsiElement objParameter : ((FunctionReference) objExpression).getParameters()) {
                intArgumentsCost += getExpressionCost(objParameter, functionsSetToAllow);
            }

            /* quite complex part - differentiate methods, functions and specially type-check functions */
            if (objExpression instanceof MethodReference) {
                intArgumentsCost += getExpressionCost(((MethodReference) objExpression).getFirstPsiChild(), functionsSetToAllow);
                intArgumentsCost += 5;
            } else {
                /* type-check &co functions */
                final String functionName = ((FunctionReference) objExpression).getName();
                if (functionName == null || functionName.isEmpty() || ! functionsSetToAllow.contains(functionName)) {
                    intArgumentsCost += 5;
                }
            }

            return intArgumentsCost;
        }

        if (objExpression instanceof UnaryExpression) {
            return getExpressionCost(((UnaryExpression) objExpression).getValue(), functionsSetToAllow);
        }

        if (objExpression instanceof BinaryExpression) {
            final BinaryExpression binary = (BinaryExpression) objExpression;
            return getExpressionCost(binary.getRightOperand(), functionsSetToAllow) +
                   getExpressionCost(binary.getLeftOperand(), functionsSetToAllow);
        }

        if (objExpression instanceof ArrayCreationExpression) {
            final ArrayCreationExpression access = (ArrayCreationExpression) objExpression;
            int intCosts = 0;
            for (final PsiElement child : access.getChildren()) {
                if (child instanceof ArrayHashElement) {
                    final ArrayHashElement pair = (ArrayHashElement) child;
                    intCosts += getExpressionCost(pair.getKey(), functionsSetToAllow);
                    intCosts += getExpressionCost(pair.getValue(), functionsSetToAllow);
                } else {
                    intCosts += getExpressionCost(child.getFirstChild(), functionsSetToAllow);
                }
            }
            return intCosts;
        }

        if (objExpression instanceof AssignmentExpression) {
            return getExpressionCost(((AssignmentExpression) objExpression).getValue(), functionsSetToAllow);
        }

        if (objExpression instanceof TernaryExpression) {
            final TernaryExpression ternary = (TernaryExpression) objExpression;
            final int intConditionCost      = getExpressionCost(ternary.getCondition(), functionsSetToAllow);
            return Math.max(
                    intConditionCost + getExpressionCost(ternary.getTrueVariant(), functionsSetToAllow),
                    intConditionCost + getExpressionCost(ternary.getFalseVariant(), functionsSetToAllow)
            );
        }

        return 10;
    }
}