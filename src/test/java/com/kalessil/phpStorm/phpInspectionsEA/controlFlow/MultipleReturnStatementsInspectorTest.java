package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MultipleReturnStatementsInspector;

final public class MultipleReturnStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(MultipleReturnStatementsInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/multiple-returns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
