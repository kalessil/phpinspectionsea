package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OnlyWritesOnParameterInspector;

final public class OnlyWritesOnParameterInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final OnlyWritesOnParameterInspector inspector = new OnlyWritesOnParameterInspector();
        inspector.IGNORE_INCLUDES                      = false;

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/deadCode/parameters-writes-only.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfRespectsSuppression() {
        myFixture.enableInspections(new OnlyWritesOnParameterInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/parameters-writes-only.suppression.php");
        myFixture.testHighlighting(true, false, true);
    }
}
