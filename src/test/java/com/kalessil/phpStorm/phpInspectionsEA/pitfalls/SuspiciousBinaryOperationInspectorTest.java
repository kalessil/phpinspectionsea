package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.SuspiciousBinaryOperationInspector;

final public class SuspiciousBinaryOperationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/suspicious-binary-operations.php");
        myFixture.enableInspections(SuspiciousBinaryOperationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
