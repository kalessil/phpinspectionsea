package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnusedGotoLabelInspector;

final public class UnusedGotoLabelInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnusedGotoLabelInspector());
        myFixture.configureByFile("fixtures/deadCode/unused-goto.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/deadCode/unused-goto.fixed.php");
    }
}
