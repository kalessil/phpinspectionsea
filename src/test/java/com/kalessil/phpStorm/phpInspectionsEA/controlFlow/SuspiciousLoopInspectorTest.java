package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.SuspiciousLoopInspector;

final public class SuspiciousLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsBasicPatterns() {
        myFixture.enableInspections(new SuspiciousLoopInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/suspicious-loop-general.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsBoundaryPatterns() {
        myFixture.enableInspections(new SuspiciousLoopInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/suspicious-loop-boundaries.php");
        myFixture.testHighlighting(true, false, true);
    }
}
