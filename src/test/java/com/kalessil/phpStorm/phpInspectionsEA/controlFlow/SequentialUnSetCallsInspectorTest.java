package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.SequentialUnSetCallsInspector;

final public class SequentialUnSetCallsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/unset-sequential.php");
        myFixture.enableInspections(SequentialUnSetCallsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
