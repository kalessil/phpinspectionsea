package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MultiAssignmentsUsageInspector;

final public class MultiAssignmentsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MultiAssignmentsUsageInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/multi-assignments.php");
        myFixture.testHighlighting(true, false, true);
    }
}
