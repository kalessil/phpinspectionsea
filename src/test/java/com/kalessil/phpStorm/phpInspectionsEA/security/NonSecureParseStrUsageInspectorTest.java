package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureParseStrUsageInspector;

final public class NonSecureParseStrUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/security/parse-str.php");
        myFixture.enableInspections(NonSecureParseStrUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
