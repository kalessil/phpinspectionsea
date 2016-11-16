package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SummerTimeUnsafeTimeManipulationInspector;

final public class SummerTimeUnsafeTimeManipulationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/summer-time.php");
        myFixture.enableInspections(SummerTimeUnsafeTimeManipulationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
