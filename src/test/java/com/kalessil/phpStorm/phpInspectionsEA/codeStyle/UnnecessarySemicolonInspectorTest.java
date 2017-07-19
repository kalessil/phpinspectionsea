package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessarySemicolonInspector;

final public class UnnecessarySemicolonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/semicolons.php");
        myFixture.enableInspections(UnnecessarySemicolonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
