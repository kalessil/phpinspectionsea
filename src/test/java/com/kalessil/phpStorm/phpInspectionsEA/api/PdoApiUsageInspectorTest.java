package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.pdo.PdoApiUsageInspector;

final public class PdoApiUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new PdoApiUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/pdo.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/pdo.fixed.php");
    }
}
