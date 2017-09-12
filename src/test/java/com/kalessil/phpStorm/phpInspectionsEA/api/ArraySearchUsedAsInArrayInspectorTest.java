package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArraySearchUsedAsInArrayInspector;

final public class ArraySearchUsedAsInArrayInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArraySearchUsedAsInArrayInspector());
        myFixture.configureByFile("fixtures/api/array-search-used-as-in-array.php");
        myFixture.testHighlighting(true, false, true);
    }
}
