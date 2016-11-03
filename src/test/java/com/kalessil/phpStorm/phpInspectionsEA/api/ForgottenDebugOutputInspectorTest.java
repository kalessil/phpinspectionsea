package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug.ForgottenDebugOutputInspector;

public class ForgottenDebugOutputInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/forgotten-debug-statements.php");
        myFixture.enableInspections(ForgottenDebugOutputInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/forgotten-debug-statements-false-positives.php");
        myFixture.enableInspections(ForgottenDebugOutputInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
