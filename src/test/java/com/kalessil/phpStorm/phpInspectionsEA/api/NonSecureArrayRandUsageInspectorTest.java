package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureArrayRandUsageInspector;

final public class NonSecureArrayRandUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureArrayRandUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/array_rand.php");
        myFixture.testHighlighting(true, false, true);
    }
}
