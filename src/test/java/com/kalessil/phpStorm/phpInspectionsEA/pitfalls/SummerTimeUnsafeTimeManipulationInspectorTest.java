package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SummerTimeUnsafeTimeManipulationInspector;

final public class SummerTimeUnsafeTimeManipulationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SummerTimeUnsafeTimeManipulationInspector());

        myFixture.configureByFile("fixtures/pitfalls/summer-time.php");
        myFixture.testHighlighting(true, false, true);
    }
}
