package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.RepetitiveMethodCallsInspector;

public class RepetitiveMethodCallsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new RepetitiveMethodCallsInspector());
        myFixture.configureByFile("fixtures/controlFlow/repetitive-method-calls.php");
        myFixture.testHighlighting(true, false, true);

    }
}
