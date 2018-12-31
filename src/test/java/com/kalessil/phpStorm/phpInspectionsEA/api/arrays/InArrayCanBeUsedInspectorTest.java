package com.kalessil.phpStorm.phpInspectionsEA.api.arrays;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.InArrayCanBeUsedInspector;

public class InArrayCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new InArrayCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/array/in_array-from-conditions.php");
        myFixture.testHighlighting(true, false, true);
    }
}
