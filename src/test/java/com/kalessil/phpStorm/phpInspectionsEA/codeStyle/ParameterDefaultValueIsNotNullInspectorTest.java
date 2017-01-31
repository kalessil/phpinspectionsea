package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ParameterDefaultValueIsNotNullInspector;

final public class ParameterDefaultValueIsNotNullInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/parameters-default-not-null.php");
        myFixture.enableInspections(ParameterDefaultValueIsNotNullInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
