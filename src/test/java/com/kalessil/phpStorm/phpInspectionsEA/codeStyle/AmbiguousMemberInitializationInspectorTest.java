package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.AmbiguousMemberInitializationInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.SuspiciousAssignmentsInspector;

final public class AmbiguousMemberInitializationInspectorTest extends CodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/property-null-initialization.php");
        myFixture.enableInspections(AmbiguousMemberInitializationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testPropertyOverridePatterns() {
        myFixture.configureByFile("fixtures/codeStyle/property-initialization-override.php");
        myFixture.enableInspections(AmbiguousMemberInitializationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}