package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.AccessModifierPresentedInspector;

public final class AccessModifierPresentedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES = true;

        myFixture.configureByFile("fixtures/classes/access-modifiers.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/classes/access-modifiers.fixed.php");
    }

    public void testIfFindsAllPatterns_PHP710() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        final AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES = true;

        myFixture.configureByFile("fixtures/classes/access-modifiers.710.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/classes/access-modifiers.710.fixed.php");
    }
}
