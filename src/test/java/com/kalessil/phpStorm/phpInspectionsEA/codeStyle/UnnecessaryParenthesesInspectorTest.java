package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryParenthesesInspector;

final public class UnnecessaryParenthesesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/codeStyle/unnecessary-parentheses.php");
        myFixture.enableInspections(UnnecessaryParenthesesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
