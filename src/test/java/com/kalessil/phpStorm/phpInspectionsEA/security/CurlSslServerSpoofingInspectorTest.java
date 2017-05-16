package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CurlSslServerSpoofingInspector;

final public class CurlSslServerSpoofingInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CurlSslServerSpoofingInspector());

        myFixture.configureByFile("fixtures/security/curl-ssl-spoofing.php");
        myFixture.testHighlighting(true, false, true);
    }
}
