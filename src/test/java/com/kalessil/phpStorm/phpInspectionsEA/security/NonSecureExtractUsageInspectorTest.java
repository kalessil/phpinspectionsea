package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureExtractUsageInspector;

final public class NonSecureExtractUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/extract.php");
        myFixture.enableInspections(NonSecureExtractUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
