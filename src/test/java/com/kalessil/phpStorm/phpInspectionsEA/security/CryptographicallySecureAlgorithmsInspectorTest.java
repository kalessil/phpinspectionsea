package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CryptographicallySecureAlgorithmsInspector;

final public class CryptographicallySecureAlgorithmsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CryptographicallySecureAlgorithmsInspector());

        myFixture.configureByFile("fixtures/security/weak-algorithms.php");
        myFixture.testHighlighting(true, false, true);
    }
}
