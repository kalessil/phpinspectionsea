package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ParameterEqualsDefaultValueInspector;

public final class ParameterEqualsDefaultValueInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/parameters-default-equals.php");
        myFixture.enableInspections(ParameterEqualsDefaultValueInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
