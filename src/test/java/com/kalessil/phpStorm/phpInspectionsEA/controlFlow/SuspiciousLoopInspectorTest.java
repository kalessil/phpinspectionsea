package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousLoopInspector;

public class SuspiciousLoopInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/suspicious-loop.php");
        myFixture.enableInspections(SuspiciousLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}