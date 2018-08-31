package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UsingInclusionReturnValueInspector;

final public class UsingInclusionReturnValueInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UsingInclusionReturnValueInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/using-inclusion-result.php");
        myFixture.testHighlighting(true, false, true);
    }
}
