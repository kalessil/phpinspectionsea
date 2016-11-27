package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CryptographicallySecureRandomnessInspector;


public class CryptographicallySecureRandomnessInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/crypto-secure-randomness.php");
        myFixture.enableInspections(CryptographicallySecureRandomnessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testModernizePatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/security/crypto-secure-randomness-php7.php");
        myFixture.enableInspections(CryptographicallySecureRandomnessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
