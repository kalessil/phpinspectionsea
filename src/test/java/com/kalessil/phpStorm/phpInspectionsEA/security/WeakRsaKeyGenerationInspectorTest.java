package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.WeakRsaKeyGenerationInspector;

final public class WeakRsaKeyGenerationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new WeakRsaKeyGenerationInspector());
        myFixture.configureByFile("testData/fixtures/security/weak-rsa-key-generation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
