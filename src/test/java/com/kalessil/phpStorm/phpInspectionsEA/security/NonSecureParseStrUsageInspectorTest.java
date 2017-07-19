package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureParseStrUsageInspector;

final public class NonSecureParseStrUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/parse-str.php");
        myFixture.enableInspections(NonSecureParseStrUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
