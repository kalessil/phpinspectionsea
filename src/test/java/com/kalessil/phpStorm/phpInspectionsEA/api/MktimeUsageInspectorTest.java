package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.MktimeUsageInspector;

public class MktimeUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/mktime.php");
        myFixture.enableInspections(MktimeUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}