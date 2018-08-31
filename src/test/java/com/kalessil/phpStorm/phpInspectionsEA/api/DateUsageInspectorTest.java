package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateUsageInspector;


final public class DateUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DateUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/date.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/date.fixed.php");
    }
}
