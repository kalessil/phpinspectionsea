package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryEmptinessCheckInspector;

final public class UnnecessaryEmptinessCheckInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final UnnecessaryEmptinessCheckInspector inspector = new UnnecessaryEmptinessCheckInspector();
        inspector.SUGGEST_SIMPLIFICATIONS                  = true;
        inspector.REPORT_CONTROVERTIAL                     = true;
        inspector.REPORT_NON_CONTRIBUTIONG                 = true;

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/unnecessary-emptiness-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
