package com.kalessil.phpStorm.phpInspectionsEA.api.arrays;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayMapMissUseInspector;

final public class ArrayMapMissUseInspectorTest  extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArrayMapMissUseInspector());
        myFixture.configureByFile("testData/fixtures/api/array/array_map-misuse.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array/array_map-misuse.fixed.php");
    }
}
