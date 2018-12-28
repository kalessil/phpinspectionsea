package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.VariableFunctionsUsageInspector;

final public class VariableFunctionsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsPhp53() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP530);
        myFixture.enableInspections(new VariableFunctionsUsageInspector());
        myFixture.configureByFile("testData/fixtures/lang/variable-functions-php53.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsAllPatternsPhp54() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP540);
        myFixture.enableInspections(new VariableFunctionsUsageInspector());
        myFixture.configureByFile("testData/fixtures/lang/variable-functions-php54.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/variable-functions-php54.fixed.php");
    }
    public void testFalsePositives() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP530);
        myFixture.enableInspections(new VariableFunctionsUsageInspector());
        myFixture.configureByFile("testData/fixtures/lang/variable-functions-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
