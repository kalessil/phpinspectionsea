package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessarySemicolonInspector;

final public class UnnecessarySemicolonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessarySemicolonInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/semicolons.php");
        myFixture.testHighlighting(true, false, true);
    }
}
