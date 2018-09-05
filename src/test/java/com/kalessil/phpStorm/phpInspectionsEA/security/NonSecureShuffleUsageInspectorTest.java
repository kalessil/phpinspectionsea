package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureShuffleUsageInspector;

final public class NonSecureShuffleUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureShuffleUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/shuffle.php");
        myFixture.testHighlighting(true, false, true);
    }
}
