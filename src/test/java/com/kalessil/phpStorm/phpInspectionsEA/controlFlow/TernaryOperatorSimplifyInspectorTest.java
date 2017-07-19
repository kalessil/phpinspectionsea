package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TernaryOperatorSimplifyInspector;

final public class TernaryOperatorSimplifyInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/ternary-simplify.php");
        myFixture.enableInspections(TernaryOperatorSimplifyInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
