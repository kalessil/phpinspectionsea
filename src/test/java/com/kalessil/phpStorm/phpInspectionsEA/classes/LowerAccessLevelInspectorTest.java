package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.LowerAccessLevelInspector;

public class LowerAccessLevelInspectorTest extends CodeInsightFixtureTestCase {
    public void testProtectedMembersOfFinalClass() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new LowerAccessLevelInspector());

        myFixture.configureByFile("fixtures/classes/weakerAccess/protected-with-final-class.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/classes/weakerAccess/protected-with-final-class.fixed.php");
    }
    public void testProtectedFieldsInPrivateContext() {
        myFixture.enableInspections(new LowerAccessLevelInspector());

        myFixture.configureByFile("fixtures/classes/weakerAccess/protected-fields-in-private-context.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/classes/weakerAccess/protected-fields-in-private-context.fixed.php");
    }
}
