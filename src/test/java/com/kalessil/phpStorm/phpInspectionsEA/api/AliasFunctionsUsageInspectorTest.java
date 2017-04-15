package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.AliasFunctionsUsageInspector;

final public class AliasFunctionsUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(AliasFunctionsUsageInspector.class);

        myFixture.configureByFile("fixtures/api/alias-functions.ns.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/api/alias-functions.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
            "fixtures/api/alias-functions.php",
            "fixtures/api/alias-functions.fixed.php",
            false
        );

    }
}