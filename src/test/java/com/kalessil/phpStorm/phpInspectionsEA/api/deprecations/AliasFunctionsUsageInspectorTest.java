package com.kalessil.phpStorm.phpInspectionsEA.api.deprecations;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations.AliasFunctionsUsageInspector;

final public class AliasFunctionsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new AliasFunctionsUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/alias-functions.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/deprecations/alias-functions.fixed.php");

    }
    public void testIfHandlesNamespacesCorrectly() {
        myFixture.enableInspections(new AliasFunctionsUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/deprecations/alias-functions.ns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
