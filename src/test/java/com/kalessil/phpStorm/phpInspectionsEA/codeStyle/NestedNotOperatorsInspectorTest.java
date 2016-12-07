package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NestedNotOperatorsInspector;

public class NestedNotOperatorsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/nested-not-operators.php");
        myFixture.enableInspections(NestedNotOperatorsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
