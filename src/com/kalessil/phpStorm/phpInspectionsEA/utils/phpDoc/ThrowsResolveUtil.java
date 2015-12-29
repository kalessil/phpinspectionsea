package com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

final public class ThrowsResolveUtil {

    public enum ResolveType { RESOLVED, RESOLVED_INHERIT_DOC, NOT_RESOLVED }

    /**
     * Return false if doc-block is not defined
     */
    static public ResolveType resolveThrownExceptions(@NotNull final Method method, @NotNull HashSet<PhpClass> declaredExceptions) {
        PhpPsiElement previous = method.getPrevPsiSibling();
        if (!(previous instanceof PhpDocCommentImpl)) {
            return ResolveType.NOT_RESOLVED;
        }

        // find all @throws and remember FQNs, @throws can be combined with @throws
        Collection<PhpDocReturnTag> returns = PsiTreeUtil.findChildrenOfType(previous, PhpDocReturnTag.class);
        for (PhpDocReturnTag returnOrThrow : returns) {
            if (returnOrThrow.getName().equals("@throws")) {
                /* definition styles can differ: single tags, pipe concatenated or combined  */
                Collection<PhpDocTypeImpl> exceptions = PsiTreeUtil.findChildrenOfType(returnOrThrow, PhpDocTypeImpl.class);

                if (exceptions.size() > 0) {
                    for (PhpDocTypeImpl type: exceptions) {
                        PsiElement typeResolved = type.resolve();
                        if (typeResolved instanceof PhpClass) {
                            declaredExceptions.add((PhpClass) typeResolved);
                        }
                    }
                }
                exceptions.clear();
            }
        }
        returns.clear();

        // resolve inherit doc tags
        Collection<PhpDocTagImpl> tags = PsiTreeUtil.findChildrenOfType(previous, PhpDocTagImpl.class);
        for (PhpDocTagImpl tag : tags) {
            if (tag.getName().equals("@inheritdoc")) {
                resolveInheritDoc(method, declaredExceptions);
                return ResolveType.RESOLVED_INHERIT_DOC;
            }
        }
        tags.clear();

        return ResolveType.RESOLVED;
    }

    /**
     * Resolves inherit doc recursively checking supers of the method owner.
     */
    private static void resolveInheritDoc(@NotNull final Method method, @NotNull HashSet<PhpClass> declaredExceptions) {
        PhpClass clazz = method.getContainingClass();
        String methodName = method.getName();
        if (null != clazz && !StringUtil.isEmpty(methodName)) {
            for (PhpClass parent : clazz.getSupers()) {
                Method parentMethod = parent.findMethodByName(methodName);
                if (null != parentMethod) {
                    resolveThrownExceptions(parentMethod, declaredExceptions);
                }
            }
        }
    }
}
