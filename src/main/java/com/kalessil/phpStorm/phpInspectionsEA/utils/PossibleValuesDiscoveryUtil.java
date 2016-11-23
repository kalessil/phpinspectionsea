package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
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
        final boolean isVariable = expression instanceof Variable;          /* parameter defaults, assignments */
        final boolean isProperty = expression instanceof FieldReference;    /* default value discovery */
        final boolean isTernary  = expression instanceof TernaryExpression; /* recursive discovery of variants */

        /* Case 2: ternary, recursively check variants */
        if (isTernary) {
            handleTernary((TernaryExpression) expression, result);
            return result;
        }

        /* Case 3: a field/constant reference */
        if (isProperty) {
            handleFieldReference((FieldReference) expression, result);
            return result;
        }

        /* Case 4: variable reference */
        if (isVariable) {
            handleVariable((Variable) expression, result);
            return result;
        }

        /* default case: add expression itself */
        result.add(expression);
        return result;
    }

    static private void handleVariable(@NotNull Variable variable, @NotNull HashSet<PsiElement> result) {
        final String variableName = variable.getName();
        final Function callable   = StringUtil.isEmpty(variableName) ? null : ExpressionSemanticUtil.getScope(variable);
        if (null == callable) {
            return;
        }

        /* collect default value if variable is a parameter */
        for (Parameter parameter : callable.getParameters()) {
            final PsiElement defaultValue = parameter.getDefaultValue();
            if (null != defaultValue && parameter.getName().equals(variableName)) {
                result.add(defaultValue);
                break;
            }
        }

        /* TODO: find writes into the variable; incl. ternaries provided for values */
    }

    static private void handleFieldReference(@NotNull FieldReference reference, @NotNull HashSet<PsiElement> result) {
        final PsiElement resolvedReference = StringUtil.isEmpty(reference.getName()) ? null : reference.resolve();
        if (null == resolvedReference) {
            return;
        }

        if (resolvedReference instanceof Field) {
            final PsiElement defaultValue = ((Field) resolvedReference).getDefaultValue();
            if (null != defaultValue) {
                result.add(defaultValue);
            }
        }

        /* TODO: analyze writes into the property in the scope; incl. ternaries provided for values */
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
    }
}
