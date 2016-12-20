package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureCryptUsageInspector;

final public class NonSecureCryptUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/crypt.php");
        myFixture.enableInspections(NonSecureCryptUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}