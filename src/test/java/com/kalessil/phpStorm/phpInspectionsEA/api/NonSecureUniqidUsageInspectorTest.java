package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.NonSecureUniqidUsageInspector;

final public class NonSecureUniqidUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/uniqid.php");
        myFixture.enableInspections(NonSecureUniqidUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
