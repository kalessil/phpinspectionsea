package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.SlowArrayOperationsInLoopInspector;

final public class SlowArrayOperationsInLoopInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SlowArrayOperationsInLoopInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/slow-array-operations.php");
        myFixture.testHighlighting(true, false, true);
    }
}
