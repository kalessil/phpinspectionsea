package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

final public class NamedElementUtil {
    @Nullable
    static public PsiElement getNameIdentifier(@Nullable PsiNameIdentifierOwner element) {
        final PsiElement nameNode = element == null ? null : element.getNameIdentifier();
        return nameNode != null && nameNode.getTextLength() > 0 ? nameNode : null;
    }
}
