package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiPsiSearchUtil {
    /*
        finds '::' or '->' node in a method reference and returns it;
        we are aware of getReferenceType method, but we need operator itself for QF-ing
    */
    @Nullable
    public static PsiElement findResolutionOperator(@Nullable MemberReference reference) {
        if (reference != null) {
            final PhpPsiElement start = reference.getFirstPsiChild();
            final PsiElement end
                = start == null ? null : (start instanceof ClassReference ? reference.getLastChild() : start.getNextPsiSibling());
            if (start != null && end != null) {
                PsiElement current = start.getNextSibling();
                while (current != null && current != end) {
                    final IElementType nodeType = current.getNode().getElementType();
                    if (nodeType == PhpTokenTypes.ARROW || nodeType == PhpTokenTypes.SCOPE_RESOLUTION) {
                        return current;
                    }
                    current = current.getNextSibling();
                }
            }
        }
        return null;
    }

    /* NOTE: get nodes even from inner lambdas */
    public static List<PsiElement> findEqual(@NotNull PsiElement where, @NotNull PsiElement what) {
        final List<PsiElement> result = new ArrayList<>();
        if (what instanceof Variable && where instanceof Function) {
            /* TODO: implement */
            throw new RuntimeException("Implement, use PhpControlFlowUtil.getFollowingVariableAccessInstructions");
        } else {
            PsiTreeUtil.findChildrenOfType(where, what.getClass()).stream()
                .filter(expression -> OpenapiEquivalenceUtil.areEqual(what, expression))
                .forEach(result::add);
        }
        return result;
    }
}
