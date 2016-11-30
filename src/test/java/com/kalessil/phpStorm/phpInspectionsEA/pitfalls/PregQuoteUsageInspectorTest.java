package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PregQuoteUsageInspector;

final public class PregQuoteUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/preg-quote.php");
        myFixture.enableInspections(PregQuoteUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
