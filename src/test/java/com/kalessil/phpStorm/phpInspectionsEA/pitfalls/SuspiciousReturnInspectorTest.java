package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousReturnInspector;

final public class SuspiciousReturnInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(SuspiciousReturnInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/return-in-finally.php");
        myFixture.testHighlighting(true, false, true);
    }
}
