package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
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

        /* Case 1: ternary operator, recursively check variants */
        if (expression instanceof TernaryExpression) {
            handleTernary((TernaryExpression) expression, result, processed);
            return result;
        }

        /* Case 2: null coalescing operator, recursively check variants */
        if (expression instanceof BinaryExpression) {
            final BinaryExpression binary = (BinaryExpression) expression;
            if (binary.getOperationType() == PhpTokenTypes.opCOALESCE) {
                handleNullCoalesce(binary, result, processed);
                return result;
            }
        }

        /* Case 3: parameter defaults, assignments */
        if (expression instanceof Variable) {
            handleVariable((Variable) expression, result, processed);
            return result;
        }

        /* Case 4: default value discovery */
        if (expression instanceof FieldReference) {
            handleClassFieldReference((FieldReference) expression, result, processed);
            return result;
        }

        /* Case 5: constants value discovery */
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
        if (callable != null) {
            for (final Parameter parameter : callable.getParameters()) {
                if (parameter.getName().equals(variableName)) {
                    final PsiElement defaultValue = parameter.getDefaultValue();
                    if (defaultValue != null) {
                        result.add(defaultValue);
                    }
                    break;
                }
            }
            handleAssignmentsInScope(callable, variable, result, processed);
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
            final PsiElement defaultValue = ((Field) field).getDefaultValue();
            if (defaultValue != null && !defaultValue.getText().endsWith(name)) {
                result.add(defaultValue);
            }
        }
        final PhpClass clazz       = field instanceof Field ? ((Field) field).getContainingClass() : null;
        final Function constructor = clazz == null ? null : clazz.getConstructor();
        final Function callable    = ExpressionSemanticUtil.getScope(reference);
        Stream.of(callable, constructor)
                .filter(Objects::nonNull)
                .forEach(method -> handleAssignmentsInScope(method, reference, result, processed));
    }

    static private void handleTernary(
            @NotNull TernaryExpression ternary,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        Stream.of(ternary.getTrueVariant(), ternary.getFalseVariant()).filter(Objects::nonNull).forEach(variant -> {
            final Set<PsiElement> variants = discover(variant, processed);
            if (!variants.isEmpty()) {
                result.addAll(variants);
                variants.clear();
            }
        });
    }

    static private void handleNullCoalesce(
            @NotNull BinaryExpression binary,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        Stream.of(binary.getLeftOperand(), binary.getRightOperand()).filter(Objects::nonNull).forEach(variant -> {
            final Set<PsiElement> variants = discover(variant, processed);
            if (!variants.isEmpty()) {
                result.addAll(variants);
                variants.clear();
            }
        });
    }

    static private void handleAssignmentsInScope(
            @NotNull Function callable,
            @NotNull PsiElement target,
            @NotNull Set<PsiElement> result,
            @NotNull Set<PsiElement> processed
    ) {
        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(callable);
        for (final AssignmentExpression expression : PsiTreeUtil.findChildrenOfType(body, AssignmentExpression.class)) {
            if (OpenapiTypesUtil.isAssignment(expression)) {
                final PsiElement container = expression.getVariable();
                if (container != null && OpenapiEquivalenceUtil.areEqual(container, target)) {
                    /* handle multiple assignments */
                    PsiElement storedValue = expression.getValue();
                    while (storedValue != null && OpenapiTypesUtil.isAssignment(storedValue)) {
                        storedValue = ((AssignmentExpression) storedValue).getValue();
                    }
                    if (storedValue != null) {
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
}
