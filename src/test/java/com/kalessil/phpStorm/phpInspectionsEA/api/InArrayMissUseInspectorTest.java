package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.InArrayMissUseInspector;

final public class InArrayMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/in-array-misuse.php");
        myFixture.enableInspections(InArrayMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}