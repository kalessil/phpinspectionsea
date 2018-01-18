package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.raceConditions.MkdirRaceConditionInspector;

final public class MkdirRaceConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MkdirRaceConditionInspector());
        myFixture.configureByFile("fixtures/pitfalls/raceCondition/mkdir-race-conditions.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/pitfalls/raceCondition/mkdir-race-conditions.fixed.php");
    }
}