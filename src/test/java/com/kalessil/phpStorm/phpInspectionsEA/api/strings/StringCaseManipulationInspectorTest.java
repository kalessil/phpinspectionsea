package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringCaseManipulationInspector;

public class StringCaseManipulationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StringCaseManipulationInspector());

        myFixture.configureByFile("fixtures/api/strings/unnecessary-case-manipulation.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/strings/unnecessary-case-manipulation.fixed.php");
    }
}

