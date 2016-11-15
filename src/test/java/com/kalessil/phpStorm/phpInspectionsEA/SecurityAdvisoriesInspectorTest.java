package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.SecurityAdvisoriesInspector;

public class SecurityAdvisoriesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/securityAdvisories/composer.json");
        myFixture.enableInspections(SecurityAdvisoriesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}