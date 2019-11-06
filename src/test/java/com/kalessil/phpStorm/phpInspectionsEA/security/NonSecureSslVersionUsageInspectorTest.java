package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureSslVersionUsageInspector;

final public class NonSecureSslVersionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureSslVersionUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/ssl-tls-versions.php");
        myFixture.testHighlighting(true, false, true);
    }
}