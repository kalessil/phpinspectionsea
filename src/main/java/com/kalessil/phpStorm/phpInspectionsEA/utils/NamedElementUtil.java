package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NamedElementUtil {
    @Nullable
    static public PsiElement getNameIdentifier(@Nullable PsiNameIdentifierOwner element) {
        PsiElement nameNode;
        try {
            nameNode = element == null ? null : element.getNameIdentifier();
        } catch (Throwable failure) {
            nameNode = null;
        }
        return nameNode != null && nameNode.getTextLength() > 0 ? nameNode : null;
    }

    @Nullable
    static public PsiElement getNameIdentifier(@NotNull FunctionReference reference) {
        final ASTNode nameNode = reference.getNameNode();
        return nameNode == null ? null : nameNode.getPsi();
    }
}
