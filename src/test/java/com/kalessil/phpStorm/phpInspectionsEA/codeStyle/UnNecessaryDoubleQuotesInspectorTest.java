package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;

final public class UnNecessaryDoubleQuotesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/double-quotes.php");
        myFixture.enableInspections(UnNecessaryDoubleQuotesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
