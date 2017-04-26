package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PropertyInitializationFlawsInspector;

final public class PropertyInitializationFlawsInspectorTest extends CodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.optionReportInitFlaws = true;
        inspector.optionReportDefaultsFlaws = true;

        myFixture.configureByFile("fixtures/codeStyle/property-null-initialization.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
    public void testPropertyOverridePatterns() {
        PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.optionReportInitFlaws = true;
        inspector.optionReportDefaultsFlaws = true;

        myFixture.configureByFile("fixtures/codeStyle/property-initialization-override.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
