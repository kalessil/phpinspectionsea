package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.UntrustedInclusionInspector;

final public class UntrustedInclusionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UntrustedInclusionInspector());
        myFixture.configureByFile("testData/fixtures/security/untrusted-inclusion.php");
        myFixture.testHighlighting(true, false, true);
    }
}
