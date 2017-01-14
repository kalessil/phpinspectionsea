package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import org.jetbrains.annotations.NotNull;

final public class AssertStringEqualsFileStrategy extends BaseFunctionReferenceStrategy {
    @Override
    @NotNull
    protected String getRecommendedAssertionName() {
        return "assertStringEqualsFile";
    }

    @Override
    @NotNull
    protected String getTargetFunctionName() {
        return "file_get_contents";
    }
}