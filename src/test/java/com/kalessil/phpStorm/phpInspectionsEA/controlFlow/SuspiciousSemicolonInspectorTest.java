package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousSemicolonInspector;

final public class SuspiciousSemicolonInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/suspicious-semicolon.php");
        myFixture.enableInspections(SuspiciousSemicolonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}