package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryCastingInspector;

final public class UnnecessaryCastingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryCastingInspector());
        myFixture.configureByFile("testData/fixtures/types/unnecessary-casting.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/types/unnecessary-casting.fixed.php");
    }

    public void testIfFindsAllPatternsPhp8() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("8.0");
        if (level != null && level.getVersionString().equals("8.0")) {
            myFixture.enableInspections(new UnnecessaryCastingInspector());
            myFixture.configureByFile("testData/fixtures/types/unnecessary-casting.php8.php");
            myFixture.testHighlighting(true, false, true);
        }
    }
}
