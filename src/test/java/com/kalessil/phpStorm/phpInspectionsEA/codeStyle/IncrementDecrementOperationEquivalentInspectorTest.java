package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IncrementDecrementOperationEquivalentInspector;

final public class IncrementDecrementOperationEquivalentInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new IncrementDecrementOperationEquivalentInspector());

        myFixture.configureByFile("fixtures/codeStyle/increment-decrement-can-be-used.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new IncrementDecrementOperationEquivalentInspector());

        myFixture.configureByFile("fixtures/codeStyle/increment-decrement-can-be-used-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
