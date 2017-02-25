package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.strategy;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class GenerateAlternativeFromNullComparisonStrategy {
    @Nullable
    static public String generate(@NotNull TernaryExpression expression) {
        return null;
    }
}
