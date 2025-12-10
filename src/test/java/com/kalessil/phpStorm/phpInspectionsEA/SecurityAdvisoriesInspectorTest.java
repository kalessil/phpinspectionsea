package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.SecurityAdvisoriesInspector;

public final class SecurityAdvisoriesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testNoComposerJson() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/any.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testHasAdvisory() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/hasAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNotNeededAdvisory() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/needsNoAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testMissingAdvisoryAndNewSection() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/needsAdvisoriesCreatesNewSection/composer.json");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/securityAdvisories/needsAdvisoriesCreatesNewSection/composer.fixed.json");
    }
    public void testMissingAdvisoryAndExistingSection() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/needsAdvisoriesUsesExistingSection/composer.json");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/securityAdvisories/needsAdvisoriesUsesExistingSection/composer.fixed.json");
    }
    public void testLibraryType() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/libraryType/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNonMasterAdvisory() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/needsMasterAdvisories/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDevDependenciesInDevPackage() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        inspector.optionConfiguration.addAll(SecurityAdvisoriesInspector.optionConfigurationDefaults());
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/devDependenciesInDevPackage/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDevDependenciesInProdPackage() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        inspector.optionConfiguration.addAll(SecurityAdvisoriesInspector.optionConfigurationDefaults());
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/devDependenciesInProdPackage/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNoRequire() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/noRequire/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testInvalidRequireList() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/invalidRequireList/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testInvalidRequirePropertyValue() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/invalidRequirePropertyValue/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
    public void testRecognizeSecurityChecker() {
        final SecurityAdvisoriesInspector inspector = new SecurityAdvisoriesInspector();
        inspector.REPORT_MISSING_ROAVE_ADVISORIES   = true;
        inspector.REPORT_MISPLACED_DEPENDENCIES     = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/securityAdvisories/hasSecurityChecker/composer.json");
        myFixture.testHighlighting(true, false, true);
    }
}
