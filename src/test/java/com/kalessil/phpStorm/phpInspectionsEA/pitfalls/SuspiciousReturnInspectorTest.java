package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousReturnInspector;

final public class SuspiciousReturnInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/return-in-finally.php");
        myFixture.enableInspections(SuspiciousReturnInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
