package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

final public class NamedElementUtil {

    /** returns name identifier, which is valid for reporting */
    @Nullable
    static public PsiElement getNameIdentifier(@Nullable PsiNameIdentifierOwner element) {
        if (null != element) {
            PsiElement id          = element.getNameIdentifier();
            boolean isIdReportable = null != id && id.getTextLength() > 0;

            return isIdReportable ? id : null;
        }

        return null;
    }

}
