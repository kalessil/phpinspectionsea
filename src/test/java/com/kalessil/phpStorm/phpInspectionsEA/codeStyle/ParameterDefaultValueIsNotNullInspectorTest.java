package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ParameterDefaultValueIsNotNullInspector;

final public class ParameterDefaultValueIsNotNullInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ParameterDefaultValueIsNotNullInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/parameters-default-not-null.php");
        myFixture.testHighlighting(true, false, true);
    }
}
