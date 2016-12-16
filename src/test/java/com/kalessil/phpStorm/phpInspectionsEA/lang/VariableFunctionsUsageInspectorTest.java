package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.VariableFunctionsUsageInspector;

/* Test the same scenario, but for different language levels */
final public class VariableFunctionsUsageInspectorTest extends CodeInsightFixtureTestCase {

    public void testIfFindsAllPatternsPhp5() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP530);
        myFixture.configureByFile("fixtures/lang/variable-functions-can-use.php");
        myFixture.enableInspections(VariableFunctionsUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAllPatternsPhp7() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.configureByFile("fixtures/lang/variable-functions-can-use.php");
        myFixture.enableInspections(VariableFunctionsUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

}
