package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CryptographicallySecureRandomnessInspector;

final public class CryptographicallySecureRandomnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP560);
        myFixture.enableInspections(new CryptographicallySecureRandomnessInspector());
        myFixture.configureByFile("testData/fixtures/security/crypto-secure-randomness.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testModernizePatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new CryptographicallySecureRandomnessInspector());
        myFixture.configureByFile("testData/fixtures/security/crypto-secure-randomness-php7.php");
        myFixture.testHighlighting(true, false, true);
    }
}
