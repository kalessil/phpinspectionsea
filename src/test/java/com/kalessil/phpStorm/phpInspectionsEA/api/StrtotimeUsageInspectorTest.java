package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.StrtotimeUsageInspector;

final public class StrtotimeUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/strtotime-function-misuse.php");
        myFixture.enableInspections(StrtotimeUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}