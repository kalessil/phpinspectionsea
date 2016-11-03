package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;

public class OneTimeUseVariablesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/one-time-use-variables.php");
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/one-time-use-variables-false-positives.php");
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
