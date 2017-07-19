package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.EncryptionInitializationVectorRandomnessInspector;

final public class EncryptionInitializationVectorRandomnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/iv-randomness.php");
        myFixture.enableInspections(EncryptionInitializationVectorRandomnessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
