package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.InfinityLoopInspector;

final public class InfinityLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new InfinityLoopInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/infinity-loops.php");
        myFixture.testHighlighting(true, false, true);
    }
}
