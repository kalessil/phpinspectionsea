package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.AliasFunctionsUsageInspector;

final public class AliasFunctionsUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/alias-functions.php");
        myFixture.enableInspections(AliasFunctionsUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}