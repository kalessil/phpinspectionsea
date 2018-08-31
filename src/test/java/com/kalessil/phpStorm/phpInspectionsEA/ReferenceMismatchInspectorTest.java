package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference.ReferenceMismatchInspector;

final public class ReferenceMismatchInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ReferenceMismatchInspector());

        myFixture.configureByFile("testData/fixtures/reference-mismatch-foreach.php");
        myFixture.testHighlighting(true, false, true);
    }
}
