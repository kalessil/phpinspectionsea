package com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
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
        /* TODO: use method.getDocComment() */
        PhpPsiElement previous = method.getPrevPsiSibling();
        if (!(previous instanceof PhpDocComment)) {
            return ResolveType.NOT_RESOLVED;
        }

        // find all @throws and remember FQNs, @throws can be combined with @throws
        Collection<PhpDocReturnTag> returns = PsiTreeUtil.findChildrenOfType(previous, PhpDocReturnTag.class);
        if (returns.size() > 0) {
            for (PhpDocReturnTag returnOrThrow : returns) {
                if (returnOrThrow.getName().equals("@throws")) {
                    /* definition styles can differ: single tags, pipe concatenated or combined  */
                    Collection<PhpDocType> exceptions = PsiTreeUtil.findChildrenOfType(returnOrThrow, PhpDocType.class);

                    if (exceptions.size() > 0) {
                        for (PhpDocType type : exceptions) {
                            PsiElement typeResolved = type.resolve();
                            if (typeResolved instanceof PhpClass) {
                                declaredExceptions.add((PhpClass) typeResolved);
                            }
                        }
                        exceptions.clear();
                    }
                }
            }
            returns.clear();
        }

        // resolve inherit doc tags
        Collection<PhpDocTag> tags = PsiTreeUtil.findChildrenOfType(previous, PhpDocTag.class);
        if (tags.size() > 0) {
            for (PhpDocTag tag : tags) {
                /* TODO: check if we can use phpDoc.hasInheritDocTag() - string search based */
                if (tag.getName().toLowerCase().equals("@inheritdoc")) {
                    resolveInheritDoc(method, declaredExceptions);
                    return ResolveType.RESOLVED_INHERIT_DOC;
                }
            }
            tags.clear();
        }

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
