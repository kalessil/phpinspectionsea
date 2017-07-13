package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.SenselessTernaryOperatorInspector;

final public class SenselessTernaryOperatorInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SenselessTernaryOperatorInspector());

        myFixture.configureByFile("fixtures/controlFlow/ternary-senseless.php");
        myFixture.testHighlighting(true, false, true);
    }
}
