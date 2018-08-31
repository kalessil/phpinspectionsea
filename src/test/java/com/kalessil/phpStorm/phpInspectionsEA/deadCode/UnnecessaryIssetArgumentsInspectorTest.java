package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryIssetArgumentsInspector;

final public class UnnecessaryIssetArgumentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryIssetArgumentsInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/unnecessary-isset-arguments.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/deadCode/unnecessary-isset-arguments.fixed.php");
    }
}
