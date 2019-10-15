package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureExtractUsageInspector;

final public class NonSecureExtractUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureExtractUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/extract.php");
        myFixture.testHighlighting(true, false, true);
    }
}
