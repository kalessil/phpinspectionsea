package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousTernaryOperatorInspector;

final public class SuspiciousTernaryOperatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousTernaryOperatorInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-ternary-operator.php");
        myFixture.testHighlighting(true, false, true);
    }
}
