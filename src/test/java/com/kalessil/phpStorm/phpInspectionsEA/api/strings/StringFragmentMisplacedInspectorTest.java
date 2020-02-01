package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringFragmentMisplacedInspector;

final public class StringFragmentMisplacedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StringFragmentMisplacedInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/string-fragment-misplaced.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/string-fragment-misplaced.fixed.php");
    }
}