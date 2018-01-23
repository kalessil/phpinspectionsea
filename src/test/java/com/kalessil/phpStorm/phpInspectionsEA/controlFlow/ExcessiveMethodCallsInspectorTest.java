package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ExcessiveMethodCallsInspector;

public class ExcessiveMethodCallsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ExcessiveMethodCallsInspector());
        myFixture.configureByFile("fixtures/controlFlow/excessive-method-calls.php");
        myFixture.testHighlighting(true, false, true);

    }
}
