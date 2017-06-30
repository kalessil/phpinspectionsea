package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.CallableInLoopTerminationConditionInspector;

public class CallableInLoopTerminationConditionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableInLoopTerminationConditionInspector());

        myFixture.configureByFile("fixtures/semanticalAnalysis/callable-inloop-condition.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/semanticalAnalysis/callable-inloop-condition.fixed.php");
    }
}
