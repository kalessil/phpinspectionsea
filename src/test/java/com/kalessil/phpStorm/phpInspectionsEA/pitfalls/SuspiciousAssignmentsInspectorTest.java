package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.SuspiciousAssignmentsInspector;

final public class SuspiciousAssignmentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testSwitchFallThroughPatterns() {
        myFixture.enableInspections(new SuspiciousAssignmentsInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-switch.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSelfAssignmentPatterns() {
        myFixture.enableInspections(new SuspiciousAssignmentsInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-self-assignment.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testParametersOverridePatterns() {
        myFixture.enableInspections(new SuspiciousAssignmentsInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-param-override.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSuspiciousFormattingPatterns() {
        myFixture.configureByFile("testData/fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-formatting.php");
        myFixture.enableInspections(new SuspiciousAssignmentsInspector());
        myFixture.testHighlighting(true, false, true);
    }
    public void testSequentialAssignmentPatterns() {
        myFixture.configureByFile("testData/fixtures/pitfalls/suspiciousAssignments/suspicious-assignments-sequential.php");
        myFixture.enableInspections(new SuspiciousAssignmentsInspector());
        myFixture.testHighlighting(true, false, true);
    }
}
