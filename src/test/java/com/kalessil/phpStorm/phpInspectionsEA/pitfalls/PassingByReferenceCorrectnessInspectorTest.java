package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference.PassingByReferenceCorrectnessInspector;

final public class PassingByReferenceCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(PassingByReferenceCorrectnessInspector.class);
        myFixture.configureByFile("fixtures/pitfalls/passing-by-reference-correctness.php");
        myFixture.testHighlighting(true, false, true);
    }
}