package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MultipleReturnStatementsInspector;

final public class MultipleReturnStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MultipleReturnStatementsInspector());

        myFixture.configureByFile("testData/fixtures/controlFlow/multiple-returns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
