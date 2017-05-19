package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.codeInsight.intention.IntentionAction;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ReturnTypeCanBeDeclaredInspector;

public final class ReturnTypeCanBeDeclaredInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.enableInspections(ReturnTypeCanBeDeclaredInspector.class);

        myFixture.configureByFile("fixtures/lang/typeHints/return-type-hints.ns.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.configureByFile("fixtures/lang/typeHints/return-type-hints.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
            "fixtures/lang/typeHints/return-type-hints.php",
            "fixtures/lang/typeHints/return-type-hints.fixed.php",
            false
        );
    }
}
