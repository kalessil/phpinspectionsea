package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureParseStrUsageInspector;

final public class NonSecureParseStrUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureParseStrUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/parse-str.php");
        myFixture.testHighlighting(true, false, true);
    }
}
