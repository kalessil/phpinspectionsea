package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug.ForgottenDebugOutputInspector;

final public class ForgottenDebugOutputInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        ForgottenDebugOutputInspector inspector = new ForgottenDebugOutputInspector();
        inspector.migratedIntoUserSpace         = false;

        inspector.registerCustomDebugMethod("my_debug_function");

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testMethodsNameCollision() {
        ForgottenDebugOutputInspector inspector = new ForgottenDebugOutputInspector();
        inspector.migratedIntoUserSpace         = false;

        inspector.registerCustomDebugMethod("\\DebugClass1::debug");
        inspector.registerCustomDebugMethod("\\DebugClass2::debug");

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements-collisions.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        ForgottenDebugOutputInspector inspector = new ForgottenDebugOutputInspector();
        inspector.migratedIntoUserSpace         = false;

        inspector.registerCustomDebugMethod(""); // to force userspace FQNs extension

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/pitfalls/forgotten-debug-statements-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
