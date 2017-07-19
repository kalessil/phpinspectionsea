package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnNecessaryDoubleQuotesInspector;

final public class UnNecessaryDoubleQuotesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/double-quotes.php");
        myFixture.enableInspections(UnNecessaryDoubleQuotesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
