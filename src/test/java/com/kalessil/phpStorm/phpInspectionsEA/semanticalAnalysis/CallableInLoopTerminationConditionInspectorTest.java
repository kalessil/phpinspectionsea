package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.CallableInLoopTerminationConditionInspector;

public class CallableInLoopTerminationConditionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/semanticalAnalysis/callable-inloop-condition.php");
        myFixture.enableInspections(CallableInLoopTerminationConditionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
