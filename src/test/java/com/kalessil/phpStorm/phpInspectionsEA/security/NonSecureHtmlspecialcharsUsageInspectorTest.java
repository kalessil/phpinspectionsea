package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureHtmlspecialcharsUsageInspector;

public class NonSecureHtmlspecialcharsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureHtmlspecialcharsUsageInspector());
        myFixture.configureByFile("fixtures/security/htmlspecialchars.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/security/htmlspecialchars.fixed.php");
    }
}