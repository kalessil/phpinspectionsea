package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MemberReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.Nullable;

final public class OpenapiPsiSearchUtil {
    /*
        finds '::' or '->' node in a method reference and returns it;
        we are aware of getReferenceType method, but we need operator itself for QF-ing
    */
    @Nullable
    public static PsiElement findResolutionOperator(@Nullable MemberReference reference) {
        if (null != reference) {
            final PhpPsiElement start = reference.getFirstPsiChild();
            final PsiElement end
                = null == start ? null : (start instanceof ClassReference ? reference.getLastChild() : start.getNextPsiSibling());
            if (null != start && null != end) {
                PsiElement current = start.getNextSibling();
                while (null != current && current != end) {
                    final IElementType nodeType = current.getNode().getElementType();
                    if (PhpTokenTypes.ARROW == nodeType || PhpTokenTypes.SCOPE_RESOLUTION == nodeType) {
                        return current;
                    }

                    current = current.getNextSibling();
                }
            }
        }

        return null;
    }
}
