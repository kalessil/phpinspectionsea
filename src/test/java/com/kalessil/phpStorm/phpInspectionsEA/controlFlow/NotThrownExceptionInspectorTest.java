package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.NotThrownExceptionInspector;

final public class NotThrownExceptionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/not-thrown-exceptions.php");
        myFixture.enableInspections(NotThrownExceptionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}