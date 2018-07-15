package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.BypassedUrlValidationInspector;

final public class BypassedUrlValidationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new BypassedUrlValidationInspector());
        myFixture.configureByFile("fixtures/security/bypassed-url-validation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
