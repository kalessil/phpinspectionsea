package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ThrowRawExceptionInspector;

final public class ThrowRawExceptionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final ThrowRawExceptionInspector inspector = new ThrowRawExceptionInspector();
        inspector.REPORT_MISSING_ARGUMENTS         = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/codeStyle/throw-raw-exception.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/codeStyle/throw-raw-exception.fixed.php");
    }
}
