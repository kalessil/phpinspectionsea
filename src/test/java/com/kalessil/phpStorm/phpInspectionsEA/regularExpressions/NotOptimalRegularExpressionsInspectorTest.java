package com.kalessil.phpStorm.phpInspectionsEA.regularExpressions;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.NotOptimalRegularExpressionsInspector;

public class NotOptimalRegularExpressionsInspectorTest extends CodeInsightFixtureTestCase {
    public void testFindGreedyCharacterSets() {
        myFixture.configureByFile("fixtures/regularExpressions/greedy-character-sets.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}