package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureExtractUsageInspector;

public class NonSecureExtractUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/extract.php");
        myFixture.enableInspections(NonSecureExtractUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
