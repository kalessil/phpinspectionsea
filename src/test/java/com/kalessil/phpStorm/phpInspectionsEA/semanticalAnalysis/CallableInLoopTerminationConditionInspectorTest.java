package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.CallableInLoopTerminationConditionInspector;

final public class CallableInLoopTerminationConditionInspectorTest extends PhpCodeInsightFixtureTestCase {
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
