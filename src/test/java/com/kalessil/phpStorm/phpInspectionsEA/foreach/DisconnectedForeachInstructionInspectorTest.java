package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.DisconnectedForeachInstructionInspector;

final public class DisconnectedForeachInstructionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach.php");
        myFixture.enableInspections(DisconnectedForeachInstructionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach-false-positives.php");
        myFixture.enableInspections(DisconnectedForeachInstructionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

