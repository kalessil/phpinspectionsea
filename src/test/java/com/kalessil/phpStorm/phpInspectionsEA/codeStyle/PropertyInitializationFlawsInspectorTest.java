package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PropertyInitializationFlawsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TraitsPropertiesConflictsInspector;

final public class PropertyInitializationFlawsInspectorTest extends CodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                    = true;
        inspector.REPORT_DEFAULTS_FLAWS                = true;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/codeStyle/property-null-initialization.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPropertyOverridePatterns() {
        PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                    = true;
        inspector.REPORT_DEFAULTS_FLAWS                = true;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/codeStyle/property-initialization-override.php");
        myFixture.testHighlighting(true, false, true);
    }
}
