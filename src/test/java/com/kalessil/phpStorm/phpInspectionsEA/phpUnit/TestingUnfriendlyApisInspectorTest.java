package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.TestingUnfriendlyApisInspector;

final public class TestingUnfriendlyApisInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsTargetPatterns() {
        final TestingUnfriendlyApisInspector inspector = new TestingUnfriendlyApisInspector();
        inspector.COMPLAIN_THRESHOLD = 2;
        inspector.SCREAM_THRESHOLD   = 3;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/phpUnit/testing-unfriendly-apis.php");
        myFixture.testHighlighting(true, false, true);
    }
}
