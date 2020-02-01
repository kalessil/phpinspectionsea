package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.MissingHashElementArrowInspector;

final public class MissingHashElementArrowInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MissingHashElementArrowInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/missing-hash-element-arrow.php");
        myFixture.testHighlighting(true, false, true);
    }
}