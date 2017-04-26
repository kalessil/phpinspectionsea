package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.SuspiciousBinaryOperationInspector;

final public class SuspiciousBinaryOperationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(SuspiciousBinaryOperationInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/suspicious-binary-operations.php");
        myFixture.testHighlighting(true, false, true);
    }
}
