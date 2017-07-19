package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousSemicolonInspector;

final public class SuspiciousSemicolonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/suspicious-semicolon.php");
        myFixture.enableInspections(SuspiciousSemicolonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}