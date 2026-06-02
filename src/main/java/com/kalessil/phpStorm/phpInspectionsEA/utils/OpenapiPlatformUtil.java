package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class OpenapiPlatformUtil {
    public static Map<String, Class<? extends PsiElement>> classes = new HashMap<>();
    static {
        // Dropped in PS 2021.1. PhpThrow -> PhpThrowExpression
        try { classes.put("PhpThrow", (Class<? extends PsiElement>) Class.forName("com.jetbrains.php.lang.psi.elements.PhpThrow"));           } catch (final ClassNotFoundException e) {}
        try { classes.put("PhpThrow", (Class<? extends PsiElement>) Class.forName("com.jetbrains.php.lang.psi.elements.PhpThrowExpression")); } catch (final ClassNotFoundException e) {}
    }
}
