package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;

final public class OneTimeUseVariablesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final OneTimeUseVariablesInspector inspector = new OneTimeUseVariablesInspector();
        inspector.ALLOW_LONG_STATEMENTS              = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/one-time-use-variables.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/one-time-use-variables.fixed.php");
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/one-time-use-variables-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
