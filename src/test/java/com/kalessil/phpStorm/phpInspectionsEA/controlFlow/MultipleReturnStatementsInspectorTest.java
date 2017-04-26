package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MultipleReturnStatementsInspector;

final public class MultipleReturnStatementsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(MultipleReturnStatementsInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/multiple-returns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
