package com.kalessil.phpStorm.phpInspectionsEA.api.arrays;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ExplodeMissUseInspector;

final public class ExplodeMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ExplodeMissUseInspector());
        myFixture.configureByFile("testData/fixtures/api/array/explode-misuse.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array/explode-misuse.fixed.php");
    }
}

