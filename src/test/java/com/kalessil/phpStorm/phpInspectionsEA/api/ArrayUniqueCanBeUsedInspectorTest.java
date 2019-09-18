package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayUniqueCanBeUsedInspector;

final public class ArrayUniqueCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.2");
        if (level != null && level.getVersionString().equals("7.2")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            myFixture.enableInspections(new ArrayUniqueCanBeUsedInspector());
            myFixture.configureByFile("testData/fixtures/api/array-unique.php");
            myFixture.testHighlighting(true, false, true);

            myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
            myFixture.setTestDataPath(".");
            myFixture.checkResultByFile("testData/fixtures/api/array-unique.fixed.php");
        }
    }
}
