package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.LoopWhichDoesNotLoopInspector;

final public class LoopWhichDoesNotLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/loop-does-not-loop.php");
        myFixture.enableInspections(LoopWhichDoesNotLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/controlFlow/loop-does-not-loop-false-positives.php");
        myFixture.enableInspections(LoopWhichDoesNotLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
