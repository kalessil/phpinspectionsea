package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.SecurityAdvisoriesInspector;

final public class SecurityAdvisoriesInspectorTest extends CodeInsightFixtureTestCase {
    public void testHasAdvisory() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/hasAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNotNeededAdvisory() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/needsNoAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testMissingAdvisory() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/needsAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNonMasterAdvisory() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/needsMasterAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDevDependenciesInDevPackage() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/devDependenciesInDevPackage/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDevDependenciesInProdPackage() {
        myFixture.enableInspections(new SecurityAdvisoriesInspector());

        myFixture.configureByFile("fixtures/securityAdvisories/devDependenciesInProdPackage/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
}