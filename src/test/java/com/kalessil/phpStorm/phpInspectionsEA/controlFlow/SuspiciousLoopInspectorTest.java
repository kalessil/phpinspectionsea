package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousLoopInspector;

final public class SuspiciousLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsBasicPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/suspicious-loop-general.php");
        myFixture.enableInspections(SuspiciousLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsBoundaryPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/suspicious-loop-boundaries.php");
        myFixture.enableInspections(SuspiciousLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}