package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.suspiciousAssignments.SuspiciousAssignmentsInspector;

final public class SuspiciousAssignmentsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/suspicious-assignments.php");
        myFixture.enableInspections(SuspiciousAssignmentsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}