package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.KeysFragmentationWithArrayFunctionsInspector;

final public class KeysFragmentationWithArrayFunctionsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new KeysFragmentationWithArrayFunctionsInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/array-functions-keys-fragmentation.php");
        myFixture.testHighlighting(true, false, true);
    }
}