package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArraySearchUsedAsInArrayInspector;

final public class ArraySearchUsedAsInArrayInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArraySearchUsedAsInArrayInspector());
        myFixture.configureByFile("testData/fixtures/api/array-search-used-as-in-array.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array-search-used-as-in-array.fixed.php");
    }
}
