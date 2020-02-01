package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.HashTimingAttacksInspector;

final public class HashTimingAttacksInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new HashTimingAttacksInspector());
        myFixture.configureByFile("testData/fixtures/security/hash-timing-attack.php");
        myFixture.testHighlighting(true, false, true);
    }
}