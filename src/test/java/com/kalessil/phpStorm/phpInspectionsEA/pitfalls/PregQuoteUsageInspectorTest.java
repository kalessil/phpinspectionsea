package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PregQuoteUsageInspector;

final public class PregQuoteUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/preg-quote.php");
        myFixture.enableInspections(PregQuoteUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
