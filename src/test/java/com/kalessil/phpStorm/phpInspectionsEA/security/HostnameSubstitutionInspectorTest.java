package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.HostnameSubstitutionInspector;

final public class HostnameSubstitutionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new HostnameSubstitutionInspector());

        myFixture.configureByFile("testData/fixtures/security/hostname-substitution.php");
        myFixture.testHighlighting(true, false, true);
    }
}
