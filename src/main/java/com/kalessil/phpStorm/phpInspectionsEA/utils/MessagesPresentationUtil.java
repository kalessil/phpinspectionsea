package com.kalessil.phpStorm.phpInspectionsEA.utils;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MessagesPresentationUtil {
    static public String prefixWithEa(@NotNull String message) {
        return "[EA] " + message;
    }
}
