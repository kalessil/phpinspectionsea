package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.KeysFragmentationWithArrayFilterInspector;

final public class KeysFragmentationWithArrayFilterInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new KeysFragmentationWithArrayFilterInspector());
        myFixture.configureByFile("fixtures/pitfalls/array-filter-keys-fragmentation.php");
        myFixture.testHighlighting(true, false, true);
    }
}