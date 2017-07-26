package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ParameterDefaultValueIsNotNullInspector;

final public class ParameterDefaultValueIsNotNullInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/parameters-default-not-null.php");
        myFixture.enableInspections(ParameterDefaultValueIsNotNullInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
