package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug.ForgottenDebugOutputInspector;

final public class ForgottenDebugOutputInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements.php");
        myFixture.enableInspections(ForgottenDebugOutputInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testMethodsNameCollision() {
        ForgottenDebugOutputInspector inspector = new ForgottenDebugOutputInspector();
        inspector.registerCustomDebugMethod("\\DebugClass1::debug");
        inspector.registerCustomDebugMethod("\\DebugClass2::debug");

        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements-collisions.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements-false-positives.php");
        myFixture.enableInspections(ForgottenDebugOutputInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
