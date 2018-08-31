package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.EncryptionInitializationVectorRandomnessInspector;

final public class EncryptionInitializationVectorRandomnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new EncryptionInitializationVectorRandomnessInspector());
        myFixture.configureByFile("testData/fixtures/security/iv-randomness.php");
        myFixture.testHighlighting(true, false, true);
    }
}
