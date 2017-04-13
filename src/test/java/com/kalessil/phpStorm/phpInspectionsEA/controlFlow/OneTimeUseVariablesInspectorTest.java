package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;

final public class OneTimeUseVariablesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/one-time-use-variables.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/one-time-use-variables.fixed.php");
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/controlFlow/one-time-use-variables-false-positives.php");
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
