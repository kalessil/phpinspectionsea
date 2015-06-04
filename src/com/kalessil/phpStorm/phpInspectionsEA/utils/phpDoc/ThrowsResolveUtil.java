package com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocExpectedExceptionImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

final public class ThrowsResolveUtil {

    public static enum ResolveType { RESOLVED, RESOLVED_INHERIT_DOC, NOT_RESOLVED }

    /**
     * Return false if doc-block is not defined
     */
    static public ResolveType resolveThrownExceptions(@NotNull final Method method, @NotNull HashSet<String> declaredExceptions) {
        PhpPsiElement previous = method.getPrevPsiSibling();
        if (!(previous instanceof PhpDocCommentImpl)) {
            return ResolveType.NOT_RESOLVED;
        }

        // resolve inherit doc tags
        Collection<PhpDocTagImpl> tags = PsiTreeUtil.findChildrenOfType(previous, PhpDocTagImpl.class);
        for (PhpDocTagImpl tag : tags) {
            if (tag.getName().equals("@inheritdoc")) {
                resolveInheritDoc(method, declaredExceptions);
                return ResolveType.RESOLVED_INHERIT_DOC;
            }
        }
        tags.clear();

        // find all @throws and remember FQNs
        Collection<PhpDocReturnTag> returns = PsiTreeUtil.findChildrenOfType(previous, PhpDocReturnTag.class);
        for (PhpDocReturnTag returnOrThrow : returns) {
            if (returnOrThrow.getName().equals("@throws") && returnOrThrow.getFirstPsiChild() instanceof PhpDocType) {
                PhpDocType type = (PhpDocType) returnOrThrow.getFirstPsiChild();
                declaredExceptions.add(type.getFQN());
                declaredExceptions.add(type.getName());
            }
        }
        returns.clear();

        // find all @expectedException and remember FQNs
        Collection<PhpDocExpectedExceptionImpl> expectsTags = PsiTreeUtil.findChildrenOfType(previous, PhpDocExpectedExceptionImpl.class);
        for (PhpDocExpectedExceptionImpl expect : expectsTags) {
            if (expect.getFirstPsiChild() instanceof PhpDocType) {
                PhpDocType type = (PhpDocType) expect.getFirstPsiChild();
                declaredExceptions.add(type.getFQN());
                declaredExceptions.add(type.getName());
            }
        }
        expectsTags.clear();

        return ResolveType.RESOLVED;
    }

    /**
     * Resolves inherit doc recursively checking supers of the method owner.
     */
    private static void resolveInheritDoc(@NotNull final Method method, @NotNull HashSet<String> declaredExceptions) {
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
