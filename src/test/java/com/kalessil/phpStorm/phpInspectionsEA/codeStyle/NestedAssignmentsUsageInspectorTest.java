package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.NestedAssignmentsUsageInspector;

final public class NestedAssignmentsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NestedAssignmentsUsageInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/nested-assignments.php");
        myFixture.testHighlighting(true, false, true);
    }
}
