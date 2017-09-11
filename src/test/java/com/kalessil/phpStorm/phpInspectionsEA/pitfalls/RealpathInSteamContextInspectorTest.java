package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.RealpathInSteamContextInspector;

final public class RealpathInSteamContextInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new RealpathInSteamContextInspector());
        myFixture.configureByFile("fixtures/pitfalls/realpath-in-stream-context.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/pitfalls/realpath-in-stream-context.fixed.php");
    }
}
