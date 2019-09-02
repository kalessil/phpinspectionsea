package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ConstantCanBeUsedInspector;

final public class ConstantCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ConstantCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/constants-usage.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/constants-usage.fixed.php");
    }
    public void testIfFindsOsFamilyPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.3");
        if (level != null && level.getVersionString().equals("7.3")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            myFixture.enableInspections(new ConstantCanBeUsedInspector());
            myFixture.configureByFile("testData/fixtures/api/constants-usage.php-os-family.php");
            myFixture.testHighlighting(true, false, true);
        }
    }
}
