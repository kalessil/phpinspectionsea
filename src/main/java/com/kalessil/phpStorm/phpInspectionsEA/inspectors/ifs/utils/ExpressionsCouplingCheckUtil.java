package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

final public class ExpressionsCouplingCheckUtil {
    public static boolean isSecondCoupledWithFirst(@NotNull PsiElement first, @NotNull PsiElement second) {
        boolean isCoupled                             = false;
        final HashSet<PsiElement> expressionsInSecond = new HashSet<>();

        /* TODO: non-static method/property */
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
                for (PsiElement expression : expressionsInSecond) {
                    /* find expression in first, stop processing if found match */
                    final Collection<PsiElement> findings = PsiTreeUtil.findChildrenOfType(first, expression.getClass());
                    if (findings.size() > 0) {
                        for (PsiElement subject : findings){
                            /* if subject[], do not process it */
                            final PsiElement parent = subject.getParent();
                            if (parent instanceof ArrayAccessExpression && subject == ((ArrayAccessExpression) parent).getValue()) {
                                continue;
                            }

                            if (PsiEquivalenceUtil.areElementsEquivalent(subject, expression)) {
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

        return isCoupled;
    }
}
