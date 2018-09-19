package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryEmptinessCheckInspector;

final public class UnnecessaryEmptinessCheckInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        final UnnecessaryEmptinessCheckInspector inspector = new UnnecessaryEmptinessCheckInspector();
        inspector.SUGGEST_SIMPLIFICATIONS                  = true;
        inspector.REPORT_CONTROVERTIAL                     = true;
        inspector.REPORT_NON_CONTRIBUTIONG                 = true;

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/unnecessary-emptiness-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
