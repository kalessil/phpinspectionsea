package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PdoApiUsageInspector;

final public class PdoApiUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/pdo.php");
        myFixture.enableInspections(PdoApiUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
