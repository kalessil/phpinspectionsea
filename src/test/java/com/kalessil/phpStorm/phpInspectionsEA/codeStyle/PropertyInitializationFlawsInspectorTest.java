package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PropertyInitializationFlawsInspector;

final public class PropertyInitializationFlawsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-null-initialization.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/property-null-initialization.fixed.php");
    }
    public void testPropertyOverridePatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-initialization-override.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testClassReferenceResolvingPatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-initialization-class-references.php");
        myFixture.testHighlighting(true, false, true);
    }
}
