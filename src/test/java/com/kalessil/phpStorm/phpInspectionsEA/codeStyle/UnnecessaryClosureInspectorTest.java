package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryClosureInspector;

final public class UnnecessaryClosureInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryClosureInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/unnecessary-closures.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/unnecessary-closures.fixed.php");
    }
    public void testIfFindsArrowFunctionPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.4");
        if (level != null && level.getVersionString().equals("7.4")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            myFixture.enableInspections(new UnnecessaryClosureInspector());
            myFixture.configureByFile("testData/fixtures/codeStyle/unnecessary-closures.php74.php");
            myFixture.testHighlighting(true, false, true);
        }
    }
}
