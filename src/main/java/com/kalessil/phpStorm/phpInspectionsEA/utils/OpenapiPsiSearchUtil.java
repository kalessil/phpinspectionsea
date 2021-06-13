package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

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
            if (start != null) {
                final PsiElement end = reference instanceof FieldReference ? reference.getLastChild() : start.getNextPsiSibling();
                if (end != null) {
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
        }
        return null;
    }

    @Nullable
    public static PsiElement findAssignmentOperator(@Nullable AssignmentExpression assignment) {
        if (assignment != null) {
            final PsiElement value = assignment.getValue();
            if (value != null) {
                PsiElement current = assignment.getVariable();
                while (current != null && current != value) {
                    if (current.getNode().getElementType() == PhpTokenTypes.opASGN) {
                        return current;
                    }
                    current = current.getNextSibling();
                }
            }

        }
        return null;
    }
}
