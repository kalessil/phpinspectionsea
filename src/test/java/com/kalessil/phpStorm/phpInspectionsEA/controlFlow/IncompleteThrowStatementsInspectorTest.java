package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.IncompleteThrowStatementsInspector;

final public class IncompleteThrowStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/incomplete-throw-statements.php");
        myFixture.enableInspections(IncompleteThrowStatementsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}