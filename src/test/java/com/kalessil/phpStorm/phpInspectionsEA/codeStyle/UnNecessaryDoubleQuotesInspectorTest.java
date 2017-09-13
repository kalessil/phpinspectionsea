package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnNecessaryDoubleQuotesInspector;

final public class UnNecessaryDoubleQuotesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnNecessaryDoubleQuotesInspector());
        myFixture.configureByFile("fixtures/codeStyle/double-quotes.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/codeStyle/double-quotes.fixed.php");

    }
}
