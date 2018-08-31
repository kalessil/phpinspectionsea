package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DegradedSwitchInspector;

final public class DegradedSwitchInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DegradedSwitchInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/degraded-switch.php");
        myFixture.testHighlighting(true, false, true);
   }
}
