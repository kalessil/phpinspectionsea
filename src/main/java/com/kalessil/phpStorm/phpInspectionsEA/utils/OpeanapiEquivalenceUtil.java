package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpeanapiEquivalenceUtil {
    public static boolean areEqual(@NotNull PsiElement first, @NotNull PsiElement second) {
        return first == second ||
               PsiEquivalenceUtil.areElementsEquivalent(first, second) ||
               (first.getClass() == second.getClass() && first.getText().equals(second.getText()));
    }
}
