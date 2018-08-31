package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousSemicolonInspector;

final public class SuspiciousSemicolonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousSemicolonInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/suspicious-semicolon.php");
        myFixture.testHighlighting(true, false, true);
    }
}
