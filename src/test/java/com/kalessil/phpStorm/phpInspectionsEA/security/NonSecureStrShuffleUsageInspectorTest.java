package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureStrShuffleUsageInspector;

final public class NonSecureStrShuffleUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NonSecureStrShuffleUsageInspector());
        myFixture.configureByFile("testData/fixtures/security/str-shuffle.php");
        myFixture.testHighlighting(true, false, true);
    }
}

