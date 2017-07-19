package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.IncorrectRandomRangeInspector;

final public class IncorrectRandomRangeInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/incorrect-random-range.php");
        myFixture.enableInspections(IncorrectRandomRangeInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}