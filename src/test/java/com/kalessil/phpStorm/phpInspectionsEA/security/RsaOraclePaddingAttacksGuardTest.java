package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.RsaOraclePaddingAttacksGuard;

final public class RsaOraclePaddingAttacksGuardTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new RsaOraclePaddingAttacksGuard());
        myFixture.configureByFile("fixtures/security/openssl-rsa-oracle-padding-attack.php");
        myFixture.testHighlighting(true, false, true);
    }
}
