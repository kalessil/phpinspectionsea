package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import org.jetbrains.annotations.NotNull;

final public class AssertCountStrategy extends BaseSameEqualsFunctionReferenceStrategy {
    @NotNull
    @Override
    protected String getRecommendedAssertionName() {
        return "assertCount";
    }

    @NotNull
    @Override
    protected String getTargetFunctionName() {
        return "count";
    }

    @Override
    protected boolean isTargetFunctionProcessesGivenValue() {
        return false;
    }
}