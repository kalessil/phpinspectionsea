package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.PreloadingUsageCorrectnessInspector;

final public class PreloadingUsageCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindsAllPatterns() {
        myFixture.enableInspections(new PreloadingUsageCorrectnessInspector());
        myFixture.configureByFile("testData/fixtures/preload.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/preload.fixed.php");
    }
}
