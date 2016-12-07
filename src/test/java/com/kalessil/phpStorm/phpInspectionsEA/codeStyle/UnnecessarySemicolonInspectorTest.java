package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessarySemicolonInspector;

final public class UnnecessarySemicolonInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/semicolons.php");
        myFixture.enableInspections(UnnecessarySemicolonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
