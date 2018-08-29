package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.IncorrectRandomRangeInspector;

final public class IncorrectRandomRangeInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new IncorrectRandomRangeInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/incorrect-random-range.php");
        myFixture.testHighlighting(true, false, true);
    }
}
