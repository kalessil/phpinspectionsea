package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NestedTernaryOperatorInspector;

final public class NestedTernaryOperatorInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NestedTernaryOperatorInspector());

        myFixture.configureByFile("fixtures/controlFlow/ternary-nested-and-suspicious.php");
        myFixture.testHighlighting(true, false, true);
    }
}
