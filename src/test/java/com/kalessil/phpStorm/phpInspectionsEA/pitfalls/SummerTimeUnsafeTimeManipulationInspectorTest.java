package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SummerTimeUnsafeTimeManipulationInspector;

final public class SummerTimeUnsafeTimeManipulationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SummerTimeUnsafeTimeManipulationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/summer-time.php");
        myFixture.testHighlighting(true, false, true);
    }
}
