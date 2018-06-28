package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PossibleValuesDiscoveryUtil {
    @NotNull
    static public Set<PsiElement> discover(@NotNull PsiElement expression) {
        final Set<PsiElement> processed      = new HashSet<>();
        final Set<PsiElement> result         = discover(expression, processed);
        final Set<PsiElement> filteredResult = result.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        processed.clear();
        result.clear();
        return filteredResult;
    }

    @NotNull
    static private Set<PsiElement> discover(@NotNull PsiElement expression, @NotNull Set<PsiElement> processed) {
        /* un-wrap parentheses to avoid false-positives */
        expression = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression);

        /* do not process same expressions multiple times */
        Set<PsiElement> result = new HashSet<>();
        if (processed.contains(expression)) {
            return result;
        }
        processed.add(expression);

        /* Case 1: ternary, recursively check variants */
        if (expression instanceof TernaryExpression) {
            handleTernary((TernaryExpression) expression, result, processed);
            return result;
        }

        /* Case 2: parameter defaults, assignments */
        if (expression instanceof Variable) {
            handleVariable((Variable) expression, result, processed);
            return result;
        }

        /* Case 3: default value discovery */
        if (expression instanceof FieldReference) {
            handleClassFieldReference((FieldReference) expression, result, processed);
            return result;
        }

        /* Case 4: constants value discovery */
        if (expression instanceof ClassConstantReference) {
            handleClassConstantReference((ClassConstantReference) expression, result);
            return result;
        }

        /* default case: add expression itself */
        result.add(expression);
        return result;
    }

    static private void handleVariable(
            @NotNull Variable variable,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        final String variableName = variable.getName();
        final Function callable   = variableName.isEmpty() ? null : ExpressionSemanticUtil.getScope(variable);
        if (callable == null) {
            return;
        }

        /* collect default value if variable is a parameter */
        for (final Parameter parameter : callable.getParameters()) {
            if (parameter.getName().equals(variableName)) {
                final PsiElement defaultValue = parameter.getDefaultValue();
                if (defaultValue != null) {
                    result.add(defaultValue);
                }
                break;
            }
        }

        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(callable);
        for (final AssignmentExpression expression : PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class)) {
            /* TODO: multi-assignments */
            if (OpenapiTypesUtil.isAssignment(expression)) {
                final PsiElement container   = expression.getVariable();
                final PsiElement storedValue = expression.getValue();
                if (storedValue != null && container instanceof Variable) {
                    final String containerName = ((Variable) container).getName();
                    if (containerName.equals(variableName)) {
                        final Set<PsiElement> discoveredWrites = discover(storedValue, processed);
                        if (!discoveredWrites.isEmpty()) {
                            result.addAll(discoveredWrites);
                            discoveredWrites.clear();
                        }
                    }
                }
            }
        }
    }

    static private void handleClassConstantReference(
            @NotNull ClassConstantReference reference,
            @NotNull Set<PsiElement> result
    ) {
        final String name      = reference.getName();
        final PsiElement field = (name == null || name.isEmpty()) ? null : OpenapiResolveUtil.resolveReference(reference);
        if (field instanceof Field) {
            final PsiElement defaultValue = ((Field) field).getDefaultValue();
            if (defaultValue != null) {
                result.add(defaultValue);
            }
        }
    }

    static private void handleClassFieldReference(
            @NotNull FieldReference reference,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        final String name      = reference.getName();
        final PsiElement field = (name == null || name.isEmpty()) ? null : OpenapiResolveUtil.resolveReference(reference);
        if (field instanceof Field) {
            /* TODO: properties without defaults returning variable as default are difficult to identify */
            /* TODO: multi-assignments */
            final PsiElement defaultValue = ((Field) field).getDefaultValue();
            if (defaultValue != null && !defaultValue.getText().endsWith(name)) {
                result.add(defaultValue);
            }
        }

        /* TODO: inspect own constructor for overriding property there */
        final Function callable   = ExpressionSemanticUtil.getScope(reference);
        final GroupStatement body = callable == null ? null : ExpressionSemanticUtil.getGroupStatement(callable);
        for (final AssignmentExpression expression : PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class)) {
            /* TODO: multi-assignments */
            if (OpenapiTypesUtil.isAssignment(expression)) {
                final PsiElement container   = expression.getVariable();
                final PsiElement storedValue = expression.getValue();
                if (storedValue != null && container instanceof FieldReference) {
                    final String containerName = ((FieldReference) container).getName();
                    if (containerName != null && containerName.equals(name) && OpeanapiEquivalenceUtil.areEqual(container, reference)) {
                        final Set<PsiElement> discoveredWrites = discover(storedValue, processed);
                        if (!discoveredWrites.isEmpty()) {
                            result.addAll(discoveredWrites);
                            discoveredWrites.clear();
                        }
                    }
                }
            }
        }
    }

    static private void handleTernary(
            @NotNull TernaryExpression ternary,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        final PsiElement trueVariant  = ternary.getTrueVariant();
        final PsiElement falseVariant = ternary.getFalseVariant();
        if (trueVariant != null && falseVariant != null) {
            Stream.of(trueVariant, falseVariant).forEach(variant -> {
                final Set<PsiElement> variants = discover(variant, processed);
                if (!variants.isEmpty()) {
                    result.addAll(variants);
                    variants.clear();
                }
            });
        }
    }
}
