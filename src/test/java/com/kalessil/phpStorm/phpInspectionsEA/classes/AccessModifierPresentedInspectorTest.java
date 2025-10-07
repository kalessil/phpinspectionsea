package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ReturnTypeCanBeDeclaredInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.AccessModifierPresentedInspector;

final public class AccessModifierPresentedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES                     = true;
        inspector.ANALYZE_CONSTANTS                      = true;
        myFixture.configureByFile("testData/fixtures/classes/access-modifiers.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/access-modifiers.fixed.php");
    }

    public void testIfFindsAllPatterns_PHP710() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES                     = true;
        inspector.ANALYZE_CONSTANTS                      = true;
        myFixture.configureByFile("testData/fixtures/classes/access-modifiers.710.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/access-modifiers.710.fixed.php");
    }

    public void testPropertyHooks() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP840);
        myFixture.enableInspections(new AccessModifierPresentedInspector());
        myFixture.configureByFile("testData/fixtures/classes/access-modifiers.property-hooks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
