package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringCaseManipulationInspector;

final public class StringCaseManipulationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StringCaseManipulationInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/unnecessary-case-manipulation.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/unnecessary-case-manipulation.fixed.php");
    }
}

