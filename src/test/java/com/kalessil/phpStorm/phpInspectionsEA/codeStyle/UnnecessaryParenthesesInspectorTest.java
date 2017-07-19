package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryParenthesesInspector;

final public class UnnecessaryParenthesesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new UnnecessaryParenthesesInspector());

        myFixture.configureByFile("fixtures/codeStyle/unnecessary-parentheses.php");
        myFixture.testHighlighting(true, false, true);
    }
}
