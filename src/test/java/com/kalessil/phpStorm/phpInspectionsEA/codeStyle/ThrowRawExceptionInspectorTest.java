package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ThrowRawExceptionInspector;

final public class ThrowRawExceptionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(ThrowRawExceptionInspector.class);

        myFixture.configureByFile("fixtures/codeStyle/throw-raw-exception.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/codeStyle/throw-raw-exception.fixed.php");
    }
}
