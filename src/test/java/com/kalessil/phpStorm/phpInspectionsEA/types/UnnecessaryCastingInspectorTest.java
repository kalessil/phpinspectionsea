package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryCastingInspector;

import java.util.Arrays;

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
        if (level != null && level.getVersionString().equals("8.0"))  {
            // PS 2020.2 introduced very limited PHP 8 support, hence we are checking the feature availability as well
            final boolean run = Arrays.stream(PhpLanguageFeature.class.getEnumConstants()).anyMatch(v -> v.name().equals("NULLSAFE_DEREFERENCING"));
            if (run) {
                PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
                myFixture.enableInspections(new UnnecessaryCastingInspector());
                myFixture.configureByFile("testData/fixtures/types/unnecessary-casting.php8.php");
                myFixture.testHighlighting(true, false, true);
            }
        }
    }
}
