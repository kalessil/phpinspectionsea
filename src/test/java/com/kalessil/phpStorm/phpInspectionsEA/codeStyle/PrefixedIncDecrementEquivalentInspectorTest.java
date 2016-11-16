package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.PrefixedIncDecrementEquivalentInspector;

final public class PrefixedIncDecrementEquivalentInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/prefixed-increment-decrement.php");
        myFixture.enableInspections(PrefixedIncDecrementEquivalentInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/codeStyle/prefixed-increment-decrement-false-positives.php");
        myFixture.enableInspections(PrefixedIncDecrementEquivalentInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
