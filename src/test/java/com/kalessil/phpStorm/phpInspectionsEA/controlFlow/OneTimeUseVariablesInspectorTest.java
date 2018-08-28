package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;

final public class OneTimeUseVariablesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final OneTimeUseVariablesInspector inspector = new OneTimeUseVariablesInspector();
        inspector.ALLOW_LONG_STATEMENTS              = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/controlFlow/oneTimeUse/returns-throw-array-destruct.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/oneTimeUse/returns-throw-array-destruct.fixed.php");
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("fixtures/controlFlow/oneTimeUse/false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testForeach() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("fixtures/controlFlow/oneTimeUse/foreach.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/oneTimeUse/foreach.fixed.php");
    }

    public void testEcho() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("fixtures/controlFlow/oneTimeUse/echo.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/oneTimeUse/echo.fixed.php");
    }
}
