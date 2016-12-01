package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CryptographicallySecureAlgorithmsInspector;

public class CryptographicallySecureAlgorithmsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/--.php");
        myFixture.enableInspections(CryptographicallySecureAlgorithmsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
