package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PropertyUsedInPrivateContextStrategy {
    private static final String message = "...";

    public static void apply(@NotNull PhpClass clazz, @NotNull ProblemsHolder holder) {
        /* iterate class methods: find out fields context (p/p/p) and report protected fields in only private context */
    }
}
