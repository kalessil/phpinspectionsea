package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.StreamSelectUsageInspector;

final public class StreamSelectUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StreamSelectUsageInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/stream-select.php");
        myFixture.testHighlighting(true, false, true);
    }
}
