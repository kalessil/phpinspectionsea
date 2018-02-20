package com.kalessil.phpStorm.phpInspectionsEA.api.deprecations;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations.MktimeUsageInspector;

final public class MktimeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MktimeUsageInspector());
        myFixture.configureByFile("fixtures/api/deprecations/mktime.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/deprecations/mktime.fixed.php");
    }
}