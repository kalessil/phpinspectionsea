package com.kalessil.phpStorm.phpInspectionsEA.com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions.FilePutContentsRaceConditionInspector;

final public class FilePutContentsRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new FilePutContentsRaceConditionInspector());
        myFixture.configureByFile("fixtures/pitfalls/raceCondition/file_put_contents-race-conditions.php");
        myFixture.testHighlighting(true, false, true);
    }
}