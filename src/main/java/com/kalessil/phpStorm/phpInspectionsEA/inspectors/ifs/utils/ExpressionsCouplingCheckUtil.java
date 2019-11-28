package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ExpressionsCouplingCheckUtil {
    private static Set<PsiElement> extractPotentiallyMutatedExpressions(@NotNull PsiElement expression) {
        final Set<PsiElement> mutatable = new HashSet<>();
        /* case 1: from assignments */
        final Collection<AssignmentExpression> assignments = PsiTreeUtil.findChildrenOfType(expression, AssignmentExpression.class);
        if (expression instanceof AssignmentExpression) {
            assignments.add((AssignmentExpression) expression);
        }
        if (! assignments.isEmpty()) {
            /* extract all containers */
            for (final AssignmentExpression assignment : assignments) {
                if (assignment instanceof MultiassignmentExpression) {
                    mutatable.addAll(((MultiassignmentExpression) assignment).getVariables());
                } else {
                    mutatable.add(assignment.getVariable());
                }
            }
            assignments.clear();
        }
        /* case 2: from parameters by reference */
        final Collection<FunctionReference> calls = PsiTreeUtil.findChildrenOfType(expression, FunctionReference.class);
        if (expression instanceof FunctionReference) {
            calls.add((FunctionReference) expression);
        }
        if (! calls.isEmpty()) {
            for (final FunctionReference reference: calls) {
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length > 0 && Arrays.stream(arguments).anyMatch(a -> a instanceof Variable)) {
                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                    if (resolved instanceof Function) {
                        final Parameter[] parameters = ((Function) resolved).getParameters();
                        final int limit              = Math.min(arguments.length, parameters.length);
                        for (int position = 0; position < limit ; ++position) {
                            if (arguments[position] instanceof Variable && parameters[position].isPassByRef()) {
                                mutatable.add(arguments[position]);
                            }
                        }
                    }
                }
            }
            calls.clear();
        }
        return mutatable;
    }

    public static boolean isSecondCoupledWithFirst(@NotNull PsiElement first, @NotNull PsiElement second) {
        boolean isCoupled = false;

        /* Scenario 1: 1st expression contains assignment */
        final Set<PsiElement> mutatable = extractPotentiallyMutatedExpressions(first);
        if (! mutatable.isEmpty()) {
            /* now find containers usage, we can perform same class search multiple time - perhaps improvements possible */
            for (final PsiElement expression : mutatable) {
                final Class<? extends PsiElement> clazz = expression.getClass();
                final Collection<PsiElement> findings   = PsiTreeUtil.findChildrenOfType(second, clazz);
                if (second.getClass() == clazz) {
                    findings.add(second);
                }
                if (! findings.isEmpty()) {
                    for (final PsiElement subject : findings) {
                        if (OpenapiEquivalenceUtil.areEqual(subject, expression)) {
                            isCoupled = true;
                            break;
                        }
                    }
                    findings.clear();
                }
                /* inner loop found coupled expressions break this loop as well */
                if (isCoupled) {
                    break;
                }
            }
            mutatable.clear();
        }
        if (isCoupled) {
            return true;
        }


        /* Scenario 2: 2nd expression has array access, parts of which has been used in the 1st one */
        /* TODO: non-static method/property */
        final Set<PsiElement> expressionsInSecond             = new HashSet<>();
        final Collection<ArrayAccessExpression> arrayAccesses = PsiTreeUtil.findChildrenOfType(second, ArrayAccessExpression.class);
        if (second instanceof ArrayAccessExpression) {
            arrayAccesses.add((ArrayAccessExpression) second);
        }
        if (!arrayAccesses.isEmpty()) {
            /* extract array accesses, get unique variable expressions from them */
            for (ArrayAccessExpression expression : arrayAccesses) {
                /* if expression[], do not store it */
                final PsiElement parent = expression.getParent();
                if (parent instanceof ArrayAccessExpression && expression == ((ArrayAccessExpression) parent).getValue()) {
                    continue;
                }

                /* store expression and parts of chained array access expression */
                PsiElement value = expression.getValue();
                while (value instanceof ArrayAccessExpression) {
                    expressionsInSecond.add(value);
                    value =((ArrayAccessExpression) value).getValue();
                }
                expressionsInSecond.add(value);
            }

            /* if we have expressions to lookup in first one, then work  them out and release references */
            if (!expressionsInSecond.isEmpty()) {
                for (final PsiElement expression : expressionsInSecond) {
                    /* find expression in first, stop processing if found match */
                    for (final PsiElement subject : PsiTreeUtil.findChildrenOfType(first, expression.getClass())){
                        /* if subject[], do not process it */
                        final PsiElement parent = subject.getParent();
                        if (parent instanceof ArrayAccessExpression && subject == ((ArrayAccessExpression) parent).getValue()) {
                            continue;
                        }

                        if (OpenapiEquivalenceUtil.areEqual(subject, expression)) {
                            isCoupled = true;
                            break;
                        }
                    }

                    /* inner loop found coupled expressions break this loop as well */
                    if (isCoupled) {
                        break;
                    }
                }

                expressionsInSecond.clear();
            }

            arrayAccesses.clear();
        }
        if (isCoupled) {
            return true;
        }

        /* Scenario 3: the first argument is isset */
        final Set<String> dependencies    = new HashSet<>();
        final Collection<PhpIsset> issets = PsiTreeUtil.findChildrenOfType(first, PhpIsset.class);
        if (first instanceof PhpIsset) {
            issets.add((PhpIsset) first);
        }
        if (!issets.isEmpty()) {
            for (final PhpIsset isset : issets) {
                PsiTreeUtil.findChildrenOfType(isset, ArrayAccessExpression.class).forEach(array -> {
                    PsiElement container = array.getValue();
                    while (container instanceof ArrayAccessExpression) {
                        container = ((ArrayAccessExpression) container).getValue();
                    }
                    if (container instanceof Variable) {
                        dependencies.add(((Variable) container).getName());
                    }
                });
            }
            issets.clear();
        }
        /* check if second depends on any of them */
        if (!dependencies.isEmpty()) {
            isCoupled = PsiTreeUtil.findChildrenOfType(second, Variable.class).stream().anyMatch(v -> dependencies.contains(v.getName()));
            dependencies.clear();
        }

        return isCoupled;
    }
}
