package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryCastingInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

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
        PhpLanguageLevel.set(PhpLanguageLevel.PHP800);
        myFixture.enableInspections(new UnnecessaryCastingInspector());
        myFixture.configureByFile("testData/fixtures/types/unnecessary-casting.php8.php");
        myFixture.testHighlighting(true, false, true);
        PhpLanguageLevel.set(null);
    }
}
