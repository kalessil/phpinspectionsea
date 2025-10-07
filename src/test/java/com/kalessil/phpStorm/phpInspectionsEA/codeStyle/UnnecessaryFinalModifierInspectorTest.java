package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryFinalModifierInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ReturnTypeCanBeDeclaredInspector;

final public class UnnecessaryFinalModifierInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryFinalModifierInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/unnecessary-final-modifier.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/unnecessary-final-modifier.fixed.php");
    }

    public void testPropertyHooks() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP840);
        myFixture.enableInspections(new ReturnTypeCanBeDeclaredInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/unnecessary-final-modifier.property-hooks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
