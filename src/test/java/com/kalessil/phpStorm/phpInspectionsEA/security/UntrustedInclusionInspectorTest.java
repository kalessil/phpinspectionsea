package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.UntrustedInclusionInspector;

final public class UntrustedInclusionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/untrusted-inclusion.php");
        myFixture.enableInspections(UntrustedInclusionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
