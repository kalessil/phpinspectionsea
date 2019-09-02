package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.SuspiciousLoopInspector;

final public class SuspiciousLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsBasicPatterns() {
        final SuspiciousLoopInspector inspector = new SuspiciousLoopInspector();
        inspector.VERIFY_VARIABLES_OVERRIDE     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/suspicious-loop-general.php");
        myFixture.testHighlighting(true, false, true);
    }
}
