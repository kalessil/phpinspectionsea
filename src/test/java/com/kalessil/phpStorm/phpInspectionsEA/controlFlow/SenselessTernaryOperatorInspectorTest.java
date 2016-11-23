package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.SenselessTernaryOperatorInspector;

final public class SenselessTernaryOperatorInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/ternary-senseless.php");
        myFixture.enableInspections(SenselessTernaryOperatorInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
