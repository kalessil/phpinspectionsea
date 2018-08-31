package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.FixedTimeStartWithInspector;

final public class FixedTimeStartWithInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new FixedTimeStartWithInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/fixed-time-start-with.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/fixed-time-start-with.fixed.php");
    }
}
