package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.SecureCookiesTransferInspector;

final public class SecureCookiesTransferInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SecureCookiesTransferInspector());
        myFixture.configureByFile("testData/fixtures/security/secure-cookies-transfer.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/security/secure-cookies-transfer.fixed.php");
    }
    public void testIfAllowsOptionsArray() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.3");
        if (level != null && level.getVersionString().equals("7.3")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            myFixture.enableInspections(new SecureCookiesTransferInspector());
            myFixture.configureByFile("testData/fixtures/security/secure-cookies-transfer.php-73.php");
            myFixture.testHighlighting(true, false, true);
        }
    }
}
