package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ReturnTypeCanBeDeclaredInspector;

final public class ReturnTypeCanBeDeclaredInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new ReturnTypeCanBeDeclaredInspector());

        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.ns.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
            "testData/fixtures/lang/typeHints/return-type-hints.php",
            "testData/fixtures/lang/typeHints/return-type-hints.fixed.php",
            false
        );
    }
    public void testReturnTypeGeneration() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final ReturnTypeCanBeDeclaredInspector inspector = new ReturnTypeCanBeDeclaredInspector();
        inspector.LOOKUP_PHPDOC_RETURN_DECLARATIONS      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.replacement-generation-before-8.php");
        myFixture.testHighlighting(true, false, true);

        com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel.set(com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel.PHP800);
        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.replacement-generation-after-8.php");
        myFixture.testHighlighting(true, false, true);
        com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel.set(null);
    }
    public void testReturnTypeInfluencedByInterfaces() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new ReturnTypeCanBeDeclaredInspector());
        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.inheritance-influence.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testRefactoringTargetIdentification() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new ReturnTypeCanBeDeclaredInspector());
        myFixture.configureByFile("testData/fixtures/lang/typeHints/return-type-hints.refactoring-target.php");
        myFixture.testHighlighting(true, false, true);
    }
}
