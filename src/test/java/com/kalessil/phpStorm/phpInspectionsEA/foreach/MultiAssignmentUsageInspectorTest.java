package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.MultiAssignmentUsageInspector;

final public class MultiAssignmentUsageInspectorTest  extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/foreach/list-construct-in-foreach.php");
        myFixture.enableInspections(MultiAssignmentUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
