package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.IncompleteThrowStatementsInspector;

final public class IncompleteThrowStatementsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/incomplete-throw-statements.php");
        myFixture.enableInspections(IncompleteThrowStatementsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}