package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.LoopWhichDoesNotLoopInspector;

final public class LoopWhichDoesNotLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new LoopWhichDoesNotLoopInspector());
        myFixture.configureByFile("fixtures/controlFlow/loop-does-not-loop.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.enableInspections(new LoopWhichDoesNotLoopInspector());
        myFixture.configureByFile("fixtures/controlFlow/loop-does-not-loop-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
