package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureOpensslVerifyUsageInspector;

final public class NonSecureOpensslVerifyUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureOpensslVerifyUsageInspector());
        myFixture.configureByFile("fixtures/security/openssl_verify.php");
        myFixture.testHighlighting(true, false, true);
    }
}
