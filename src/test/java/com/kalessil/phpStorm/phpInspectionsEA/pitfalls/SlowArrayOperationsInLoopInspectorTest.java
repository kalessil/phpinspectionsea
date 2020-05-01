package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.SlowArrayOperationsInLoopInspector;

final public class SlowArrayOperationsInLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllGreedyPatterns() {
        myFixture.enableInspections(new SlowArrayOperationsInLoopInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/slow-array-operations.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsAllSlowPatterns() {
        myFixture.enableInspections(new SlowArrayOperationsInLoopInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/slow-array-operations.for-termination.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/pitfalls/slow-array-operations.for-termination.fixed.php");
    }
}
