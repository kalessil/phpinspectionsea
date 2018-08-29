package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureCryptUsageInspector;

final public class NonSecureCryptUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureCryptUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/crypt.php");
        myFixture.testHighlighting(true, false, true);
    }
}
