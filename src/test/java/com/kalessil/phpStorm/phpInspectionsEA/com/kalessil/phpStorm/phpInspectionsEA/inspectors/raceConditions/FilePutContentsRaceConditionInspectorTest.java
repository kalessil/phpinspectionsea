package com.kalessil.phpStorm.phpInspectionsEA.com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions.FilePutContentsRaceConditionInspector;

final public class FilePutContentsRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new FilePutContentsRaceConditionInspector());
        myFixture.configureByFile("fixtures/pitfalls/raceCondition/file_put_contents-race-conditions.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/pitfalls/raceCondition/file_put_contents-race-conditions.fixed.php");
    }
}