package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.MkdirRaceConditionInspector;

final public class MkdirRaceConditionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/mkdir-race-conditions.php");
        myFixture.enableInspections(MkdirRaceConditionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}