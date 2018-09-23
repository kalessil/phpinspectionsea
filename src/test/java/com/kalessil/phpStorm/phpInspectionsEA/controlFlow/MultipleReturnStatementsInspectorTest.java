package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MultipleReturnStatementsInspector;

final public class MultipleReturnStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final MultipleReturnStatementsInspector inspector = new MultipleReturnStatementsInspector();
        inspector.COMPLAIN_THRESHOLD                      = 3;
        inspector.SCREAM_THRESHOLD                        = 5;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/multiple-returns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
