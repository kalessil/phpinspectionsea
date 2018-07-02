package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final public class ExpressionsCouplingCheckUtil {
    public static boolean isSecondCoupledWithFirst(@NotNull PsiElement first, @NotNull PsiElement second) {
        boolean isCoupled = false;

        /* Scenario 1: 1st expression contains assignment */
        final Set<PsiElement> expressionsInFirst      = new HashSet<>();
        final Collection<AssignmentExpression> assign = PsiTreeUtil.findChildrenOfType(first, AssignmentExpression.class);
        /* the util will return an empty collection when searching inside assignment, dealing with this */
        if (first instanceof AssignmentExpression) {
            assign.add((AssignmentExpression) first);
        }
        if (assign.size() > 0) {
            /* extract all containers */
            for (AssignmentExpression expression : assign) {
                if (expression instanceof MultiassignmentExpression) {
                    expressionsInFirst.addAll(((MultiassignmentExpression) expression).getVariables());
                    continue;
                }

                expressionsInFirst.add(expression.getVariable());
            }
            assign.clear();

            if (expressionsInFirst.size() > 0) {
                /* now find containers usage, we can perform same class search multiple time - perhaps improvements possible */
                for (final PsiElement expression : expressionsInFirst) {
                    final Collection<PsiElement> findings = PsiTreeUtil.findChildrenOfType(second, expression.getClass());
                    if (findings.size() > 0) {
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
                expressionsInFirst.clear();
            }
        }
        if (isCoupled) {
            return true;
        }


        /* Scenario 2: 2nd expression has array access, parts of which has been used in the 1st one */
        /* TODO: non-static method/property */
        final Set<PsiElement> expressionsInSecond           = new HashSet<>();
        final Collection<ArrayAccessExpression> arrayAccess = PsiTreeUtil.findChildrenOfType(second, ArrayAccessExpression.class);
        if (arrayAccess.size() > 0) {
            /* extract array accesses, get unique variable expressions from them */
            for (ArrayAccessExpression expression : arrayAccess) {
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
            if (expressionsInSecond.size() > 0) {
                for (final PsiElement expression : expressionsInSecond) {
                    /* find expression in first, stop processing if found match */
                    final Collection<PsiElement> findings = PsiTreeUtil.findChildrenOfType(first, expression.getClass());
                    if (findings.size() > 0) {
                        for (PsiElement subject : findings){
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

                        findings.clear();
                    }

                    /* inner loop found coupled expressions break this loop as well */
                    if (isCoupled) {
                        break;
                    }
                }

                expressionsInSecond.clear();
            }

            arrayAccess.clear();
        }
        if (isCoupled) {
            return true;
        }

        /* Scenario 3: the first argument is isset */
        final Set<String> dependencies = new HashSet<>();
        PsiTreeUtil.findChildrenOfType(first, PhpIsset.class).forEach(
            isset -> PsiTreeUtil.findChildrenOfType(isset, ArrayAccessExpression.class).forEach(array -> {
                PsiElement container = array.getValue();
                while (container instanceof ArrayAccessExpression) {
                    container = ((ArrayAccessExpression) container).getValue();
                }
                if (container instanceof Variable) {
                    dependencies.add(((Variable) container).getName());
                }
            })
        );
        /* check if second depends on any of them */
        if (!dependencies.isEmpty()) {
            isCoupled = PsiTreeUtil.findChildrenOfType(second, Variable.class).stream().anyMatch(v -> dependencies.contains(v.getName()));
            dependencies.clear();
        }

        return isCoupled;
    }
}
