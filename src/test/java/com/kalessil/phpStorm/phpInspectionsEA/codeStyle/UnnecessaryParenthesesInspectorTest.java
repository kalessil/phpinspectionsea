package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryParenthesesInspector;

final public class UnnecessaryParenthesesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unnecessary-parentheses.php");
        myFixture.enableInspections(UnnecessaryParenthesesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
