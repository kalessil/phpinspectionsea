package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.EncryptionInitializationVectorRandomnessInspector;

public class EncryptionInitializationVectorRandomnessInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/iv-randomness.php");
        myFixture.enableInspections(EncryptionInitializationVectorRandomnessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
