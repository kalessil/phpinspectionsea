package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.AliasFunctionsUsageInspector;

final public class AliasFunctionsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new AliasFunctionsUsageInspector());
        myFixture.configureByFile("fixtures/api/alias-functions.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/alias-functions.fixed.php");

    }
    public void testIfHandlesNamespacesCorrectly() {
        myFixture.enableInspections(new AliasFunctionsUsageInspector());
        myFixture.configureByFile("fixtures/api/alias-functions.ns.php");
        myFixture.testHighlighting(true, false, true);
    }
}