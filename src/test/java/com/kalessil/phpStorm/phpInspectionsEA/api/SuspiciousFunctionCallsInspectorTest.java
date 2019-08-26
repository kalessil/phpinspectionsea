package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.SuspiciousFunctionCallsInspector;

final public class SuspiciousFunctionCallsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousFunctionCallsInspector());
        myFixture.configureByFile("testData/fixtures/api/suspicious-function-calls.php");
        myFixture.testHighlighting(true, false, true);
    }
}
