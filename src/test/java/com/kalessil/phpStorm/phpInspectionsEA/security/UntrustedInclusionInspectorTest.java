package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.UntrustedInclusionInspector;

final public class UntrustedInclusionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/untrusted-inclusion.php");
        myFixture.enableInspections(UntrustedInclusionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
