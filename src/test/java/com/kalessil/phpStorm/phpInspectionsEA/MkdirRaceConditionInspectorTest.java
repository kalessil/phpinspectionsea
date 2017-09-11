package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.MkdirRaceConditionInspector;

final public class MkdirRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MkdirRaceConditionInspector());
        myFixture.configureByFile("fixtures/pitfalls/mkdir-race-conditions.php");
        myFixture.testHighlighting(true, false, true);
    }
}