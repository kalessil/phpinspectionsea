package com.kalessil.phpStorm.phpInspectionsEA.settings;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public enum StrictnessCategory {
    STRICTNESS_CATEGORY_SECURITY("Security"),
    STRICTNESS_CATEGORY_PROBABLE_BUGS("Probable bugs"),
    STRICTNESS_CATEGORY_PERFORMANCE("Performance"),
    STRICTNESS_CATEGORY_ARCHITECTURE("Architecture"),
    STRICTNESS_CATEGORY_CONTROL_FLOW("Control flow"),
    STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION("Language level migration"),
    STRICTNESS_CATEGORY_CODE_STYLE("Code style"),
    STRICTNESS_CATEGORY_UNUSED("Unused"),
    STRICTNESS_CATEGORY_PHPUNIT("PhpUnit");

    private final String value;

    StrictnessCategory(@NotNull String value) {
        this.value = value;
    }
}
