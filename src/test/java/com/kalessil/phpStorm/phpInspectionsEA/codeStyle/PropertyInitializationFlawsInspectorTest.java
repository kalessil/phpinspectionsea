package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PropertyInitializationFlawsInspector;

final public class PropertyInitializationFlawsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/codeStyle/property-null-initialization.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPropertyOverridePatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/codeStyle/property-initialization-override.php");
        myFixture.testHighlighting(true, false, true);
    }
}
