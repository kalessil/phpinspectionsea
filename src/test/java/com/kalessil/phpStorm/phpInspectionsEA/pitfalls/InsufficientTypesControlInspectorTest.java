package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.InsufficientTypesControlInspector;

final public class InsufficientTypesControlInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new InsufficientTypesControlInspector());
        myFixture.configureByFile("fixtures/pitfalls/insufficient-types-control.php");
        myFixture.testHighlighting(true, false, true);
    }
}