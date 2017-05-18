package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.jetbrains.php.lang.inspections.PhpTooManyParametersInspection;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Community request leaded to duplicating functionality of native PS inspection, so we just inherit it.
 *  see https://bitbucket.org/kalessil/phpinspectionsea/issues/289/allow-more-than-3-parameters-in
 */
public class MoreThanThreeArgumentsInspector extends PhpTooManyParametersInspection {
    @NotNull
    public String getShortName() {
        return "MoreThanThreeArgumentsInspection";
    }

    public MoreThanThreeArgumentsInspector() {
        limit = 3;
    }
}