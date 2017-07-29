package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DefaultValueInElseBranchInspector;

final public class DefaultValueInElseBranchInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DefaultValueInElseBranchInspector());

        myFixture.configureByFile("fixtures/controlFlow/default-value-in-else.php");
        myFixture.testHighlighting(true, false, true);
    }
}