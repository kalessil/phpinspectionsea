package com.kalessil.phpStorm.phpInspectionsEA.tests.api;

import org.junit.Test;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.security.NonSecureUniqidUsageInspector;

public class NonSecureUniqidUsageInspectorTest extends CodeInsightFixtureTestCase {
    @Test
    public void testIfFindsAllPatterns() {
        myFixture.configureByFiles("fixtures/uniqid.php");
        myFixture.enableInspections(NonSecureUniqidUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
