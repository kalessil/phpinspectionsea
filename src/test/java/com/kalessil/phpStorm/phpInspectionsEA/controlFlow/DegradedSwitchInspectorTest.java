package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DegradedSwitchInspector;

public class DegradedSwitchInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DegradedSwitchInspector());
        myFixture.configureByFile("fixtures/controlFlow/degraded-switch.php");
        myFixture.testHighlighting(true, false, true);
   }
}
