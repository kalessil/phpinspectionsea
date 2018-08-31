package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CurlSslServerSpoofingInspector;

final public class CurlSslServerSpoofingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CurlSslServerSpoofingInspector());
        myFixture.configureByFile("testData/fixtures/security/curl-ssl-spoofing.php");
        myFixture.testHighlighting(true, false, true);
    }
}
