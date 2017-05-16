package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.SuspiciousAssignmentsInspector;

final public class SuspiciousAssignmentsInspectorTest extends CodeInsightFixtureTestCase {
    public void testSwitchFallThroughPatterns() {
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-switch.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSelfAssignmentPatterns() {
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-self-assignment.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testParametersOverridePatterns() {
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-param-override.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSuspiciousFormattingPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-formatting.php");
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testSequentialAssignmentPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-sequential.php");
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}