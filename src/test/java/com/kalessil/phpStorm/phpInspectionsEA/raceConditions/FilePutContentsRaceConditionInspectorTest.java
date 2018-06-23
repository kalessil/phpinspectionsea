package com.kalessil.phpStorm.phpInspectionsEA.raceConditions;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions.FilePutContentsRaceConditionInspector;

final public class FilePutContentsRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final FilePutContentsRaceConditionInspector inspector = new FilePutContentsRaceConditionInspector();
        inspector.REDUCED_SCOPE                               = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/pitfalls/raceCondition/file_put_contents-race-conditions.php");
        myFixture.testHighlighting(true, false, true);
        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/pitfalls/raceCondition/file_put_contents-race-conditions.fixed.php");
    }
}