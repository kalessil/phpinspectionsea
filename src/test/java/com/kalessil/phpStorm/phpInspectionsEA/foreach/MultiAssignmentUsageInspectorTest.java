package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.MultiAssignmentUsageInspector;

final public class MultiAssignmentUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MultiAssignmentUsageInspector());
        myFixture.configureByFile("testData/fixtures/foreach/list-construct-in-foreach.php");
        myFixture.testHighlighting(true, false, true);
    }
}
