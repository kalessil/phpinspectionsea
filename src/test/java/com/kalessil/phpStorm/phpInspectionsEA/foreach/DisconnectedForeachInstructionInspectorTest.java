package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.DisconnectedForeachInstructionInspector;

final public class DisconnectedForeachInstructionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.SUGGEST_USING_CLONE = true;

        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.SUGGEST_USING_CLONE = true;

        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach-false-positives.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}

