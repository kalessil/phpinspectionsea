package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.jetbrains.php.lang.inspections.PhpTooManyParametersInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Community request leaded to duplicating functionality of native PS inspection, so we just inherit it.
 * @see https://bitbucket.org/kalessil/phpinspectionsea/issues/289/allow-more-than-3-parameters-in
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