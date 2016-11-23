package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class PossibleValuesDiscoveryUtil {
    @NotNull
    static public HashSet<PsiElement> discover(@NotNull PsiElement expression) {
        /* un-wrap parenthesises to avoid false-positives */
        expression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression);

        HashSet<PsiElement> result = new HashSet<>();
        final boolean isTernary  = expression instanceof TernaryExpression; /* recursive discovery of variants */
        final boolean isVariable = expression instanceof Variable;          /* parameter defaults, assignments */
        final boolean isProperty = expression instanceof FieldReference;    /* default value discovery */

        /* Case 1: nothing to discover, return expression itself */
        if (!isTernary && !isVariable && !isProperty) {
            result.add(expression);
            return result;
        }

        /* Case 2: ternary, recursively check variants */
        if (isTernary) {
            handleTernary((TernaryExpression) expression, result);
            return result;
        }

        /* Case 3: a field/constant reference */
        /* TODO: analyze $htis->property modifications in the scope */
        if (isProperty) {
            handleFieldReference((FieldReference) expression, result);
            return result;
        }

        return result;
    }

    static private void handleFieldReference(@NotNull FieldReference reference, @NotNull HashSet<PsiElement> result) {
        final PsiElement resolvedReference = reference.resolve();
        if (null == resolvedReference) {
            return;
        }

        if (resolvedReference instanceof Field) {
            final PsiElement defaultValue = ((Field) resolvedReference).getDefaultValue();
            if (null != defaultValue) {
                result.add(defaultValue);
            }
        }
    }

    static private void handleTernary(@NotNull TernaryExpression ternary, @NotNull HashSet<PsiElement> result) {
        final PsiElement trueVariant  = ternary.getTrueVariant();
        final PsiElement falseVariant = ternary.getFalseVariant();
        if (null == trueVariant || null == falseVariant) {
            return;
        }

        /* discover true and false branches */
        HashSet<PsiElement> trueVariants = discover(trueVariant);
        if (trueVariants.size() > 0) {
            result.addAll(trueVariants);
            trueVariants.clear();
        }
        HashSet<PsiElement> falseVariants = discover(falseVariant);
        if (falseVariants.size() > 0) {
            result.addAll(falseVariants);
            falseVariants.clear();
        }

        return;
    }
}
