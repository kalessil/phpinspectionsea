package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.InArrayMissUseInspector;

final public class InArrayMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final InArrayMissUseInspector inspection = new InArrayMissUseInspector();
        inspection.FORCE_STRICT_COMPARISON       = false;
        myFixture.enableInspections(inspection);
        myFixture.configureByFile("testData/fixtures/api/array/in-array-misuse.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array/in-array-misuse.fixed.php");
    }
}
