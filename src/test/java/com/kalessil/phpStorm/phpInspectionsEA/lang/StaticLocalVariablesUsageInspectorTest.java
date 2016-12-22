package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.StaticLocalVariablesUsageInspector;

public class StaticLocalVariablesUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/static-local-variables.php");
        myFixture.enableInspections(StaticLocalVariablesUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/lang/static-local-variables-false-positives.php");
        myFixture.enableInspections(StaticLocalVariablesUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}