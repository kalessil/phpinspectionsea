package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureCryptUsageInspector;

final public class NonSecureCryptUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/crypt.php");
        myFixture.enableInspections(NonSecureCryptUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}