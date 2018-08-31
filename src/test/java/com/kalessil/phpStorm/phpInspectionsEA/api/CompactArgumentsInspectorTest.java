package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.CompactArgumentsInspector;

final public class CompactArgumentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CompactArgumentsInspector());
        myFixture.configureByFile("testData/fixtures/api/compact-arguments-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}
