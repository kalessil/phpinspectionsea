package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CryptographicallySecureAlgorithmsInspector;

final public class CryptographicallySecureAlgorithmsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CryptographicallySecureAlgorithmsInspector());
        myFixture.configureByFile("testData/fixtures/security/weak-algorithms.php");
        myFixture.testHighlighting(true, false, true);
    }
}
