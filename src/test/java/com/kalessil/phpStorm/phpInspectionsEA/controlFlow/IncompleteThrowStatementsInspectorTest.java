package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.IncompleteThrowStatementsInspector;

final public class IncompleteThrowStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new IncompleteThrowStatementsInspector());

        myFixture.configureByFile("testData/fixtures/controlFlow/incomplete-throw-statements.php");
        myFixture.testHighlighting(true, false, true);
    }
}
