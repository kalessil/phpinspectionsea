package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.SequentialUnSetCallsInspector;

final public class SequentialUnSetCallsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/unset-sequential.php");
        myFixture.enableInspections(SequentialUnSetCallsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
