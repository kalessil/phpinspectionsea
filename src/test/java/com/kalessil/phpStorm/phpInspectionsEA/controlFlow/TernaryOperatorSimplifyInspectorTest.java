package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TernaryOperatorSimplifyInspector;

final public class TernaryOperatorSimplifyInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/ternary-simplify.php");
        myFixture.enableInspections(TernaryOperatorSimplifyInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
