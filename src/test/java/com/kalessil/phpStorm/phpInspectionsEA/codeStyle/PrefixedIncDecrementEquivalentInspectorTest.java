package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.PrefixedIncDecrementEquivalentInspector;

final public class PrefixedIncDecrementEquivalentInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new PrefixedIncDecrementEquivalentInspector());

        myFixture.configureByFile("fixtures/codeStyle/prefixed-increment-decrement.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new PrefixedIncDecrementEquivalentInspector());

        myFixture.configureByFile("fixtures/codeStyle/prefixed-increment-decrement-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
