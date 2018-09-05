package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.RepetitiveMethodCallsInspector;

final public class RepetitiveMethodCallsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new RepetitiveMethodCallsInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/repetitive-method-calls.php");
        myFixture.testHighlighting(true, false, true);
    }
}
