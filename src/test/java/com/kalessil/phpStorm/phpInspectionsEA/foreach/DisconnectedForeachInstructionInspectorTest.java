package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.DisconnectedForeachInstructionInspector;

final public class DisconnectedForeachInstructionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.optionSuggestUsingClone = true;

        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.optionSuggestUsingClone = true;

        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach-false-positives.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}

