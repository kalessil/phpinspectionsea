package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ExplodeMissUseInspector;

final public class ExplodeMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(ExplodeMissUseInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/explode-misuse.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/explode-misuse.fixed.php");
    }
}

