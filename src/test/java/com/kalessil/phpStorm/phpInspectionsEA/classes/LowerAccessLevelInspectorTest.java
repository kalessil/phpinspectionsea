package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.LowerAccessLevelInspector;

final public class LowerAccessLevelInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testProtectedMembersOfFinalClass() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new LowerAccessLevelInspector());
        myFixture.configureByFile("testData/fixtures/classes/weakerAccess/protected-with-final-class.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/weakerAccess/protected-with-final-class.fixed.php");
    }
    public void testProtectedFieldsInPrivateContext() {
        myFixture.enableInspections(new LowerAccessLevelInspector());
        myFixture.configureByFile("testData/fixtures/classes/weakerAccess/protected-fields-in-private-context.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/weakerAccess/protected-fields-in-private-context.fixed.php");
    }
}
