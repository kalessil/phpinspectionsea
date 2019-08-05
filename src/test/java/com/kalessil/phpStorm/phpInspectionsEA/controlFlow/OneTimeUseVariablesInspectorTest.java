package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;

final public class OneTimeUseVariablesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        final OneTimeUseVariablesInspector inspector = new OneTimeUseVariablesInspector();
        inspector.ALLOW_LONG_STATEMENTS              = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/oneTimeUse/returns-throw-array-destruct.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/oneTimeUse/returns-throw-array-destruct.fixed.php");
    }
    public void testFalsePositives() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/oneTimeUse/false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testForeach() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/oneTimeUse/foreach.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/oneTimeUse/foreach.fixed.php");
    }
    public void testEcho() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/oneTimeUse/echo.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/oneTimeUse/echo.fixed.php");
    }
    public void testSequentialAssignments() {
        myFixture.enableInspections(new OneTimeUseVariablesInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/oneTimeUse/sequential-assignments.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/oneTimeUse/sequential-assignments.fixed.php");
    }
}
