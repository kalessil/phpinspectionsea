package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.RsaOraclePaddingAttacksInspector;

final public class RsaOraclePaddingAttacksInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsOpensslPatterns() {
        myFixture.enableInspections(new RsaOraclePaddingAttacksInspector());
        myFixture.configureByFile("testData/fixtures/security/openssl-rsa-oracle-padding-attack.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsMcryptPatterns() {
        myFixture.enableInspections(new RsaOraclePaddingAttacksInspector());
        myFixture.configureByFile("testData/fixtures/security/mcrypt-rsa-oracle-padding-attack.php");
        myFixture.testHighlighting(true, false, true);
    }
}
