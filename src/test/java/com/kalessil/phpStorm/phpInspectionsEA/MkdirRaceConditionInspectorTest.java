package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.MkdirRaceConditionInspector;

final public class MkdirRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MkdirRaceConditionInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/mkdir-race-conditions.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/pitfalls/mkdir-race-conditions.fixed.php");
    }
}
