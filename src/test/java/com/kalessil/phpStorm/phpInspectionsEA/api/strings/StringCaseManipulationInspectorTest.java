package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringCaseManipulationInspector;

final public class StringCaseManipulationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StringCaseManipulationInspector());
        myFixture.configureByFile("fixtures/api/strings/unnecessary-case-manipulation.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/strings/unnecessary-case-manipulation.fixed.php");
    }
}

