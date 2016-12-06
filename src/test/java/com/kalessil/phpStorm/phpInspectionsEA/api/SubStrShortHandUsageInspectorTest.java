package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos.SubStrShortHandUsageInspector;

public class SubStrShortHandUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/substr-short-hand.php");
        myFixture.enableInspections(SubStrShortHandUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}