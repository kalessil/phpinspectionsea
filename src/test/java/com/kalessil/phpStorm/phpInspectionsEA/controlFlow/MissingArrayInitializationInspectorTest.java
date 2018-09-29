package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.MissingArrayInitializationInspector;

final public class MissingArrayInitializationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MissingArrayInitializationInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/missing-array-initialization.php");
        myFixture.testHighlighting(true, false, true);
    }
}