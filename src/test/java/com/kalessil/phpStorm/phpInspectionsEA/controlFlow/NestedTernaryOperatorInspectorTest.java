package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NestedTernaryOperatorInspector;

final public class NestedTernaryOperatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NestedTernaryOperatorInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/ternary-nested-and-suspicious.php");
        myFixture.testHighlighting(true, false, true);
    }
}
