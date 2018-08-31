package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnNecessaryDoubleQuotesInspector;

final public class UnNecessaryDoubleQuotesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnNecessaryDoubleQuotesInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/double-quotes.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/double-quotes.fixed.php");
    }
}
