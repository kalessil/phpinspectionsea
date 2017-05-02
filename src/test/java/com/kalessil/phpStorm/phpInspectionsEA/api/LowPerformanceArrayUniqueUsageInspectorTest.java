package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.LowPerformanceArrayUniqueUsageInspector;

final public class LowPerformanceArrayUniqueUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(LowPerformanceArrayUniqueUsageInspector.class);

        myFixture.configureByFile("fixtures/api/array-unique.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/array-unique.fixed.php");
    }
}