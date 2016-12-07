package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.IncorrectRandomRangeInspector;

final public class IncorrectRandomRangeInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/incorrect-random-range.php");
        myFixture.enableInspections(IncorrectRandomRangeInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}