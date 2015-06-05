package com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

final public class CollectPossibleThrowsUtil {
    static public void collectAnnotatedExceptions(@NotNull final Method method, @NotNull HashSet<String> declaredExceptions) {
        // resolve inherit doc tags
        Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(method, MethodReference.class);
        for (MethodReference call : calls) {
            PsiElement methodResolved = call.resolve();
            if (methodResolved instanceof Method) {
                ThrowsResolveUtil.resolveThrownExceptions((Method) methodResolved, declaredExceptions);
            }
        }
        calls.clear();

        Collection<NewExpression> news = PsiTreeUtil.findChildrenOfType(method, NewExpression.class);
        for (NewExpression newExpression : news) {
            /* iterate all new expressions except throws  */
            if (!(newExpression.getParent() instanceof PhpThrow)) {
                ClassReference reference = newExpression.getClassReference();
                if (null != reference) {
                    PsiElement classResolved = reference.resolve();
                    if (classResolved instanceof PhpClass) {
                        /* resolved class and it's constructor, which can throw exceptions as well */
                        Method constructor = ((PhpClass) classResolved).getConstructor();
                        if (null != constructor) {
                            ThrowsResolveUtil.resolveThrownExceptions(constructor, declaredExceptions);
                        }
                    }
                }
            }
        }
        news.clear();

        // TODO: try-catch work-flows support
    }
}
