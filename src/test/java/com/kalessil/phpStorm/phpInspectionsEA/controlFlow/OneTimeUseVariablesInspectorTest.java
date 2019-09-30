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
        inspector.ANALYZE_RETURN_STATEMENTS          = true;
        inspector.ANALYZE_THROW_STATEMENTS           = true;
        inspector.ANALYZE_ARRAY_DESTRUCTURING        = true;
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
