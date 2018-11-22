package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.DisconnectedForeachInstructionInspector;

final public class DisconnectedForeachInstructionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.SUGGEST_USING_CLONE                           = true;
        myFixture.configureByFile("testData/fixtures/foreach/disconnected-statements-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        final DisconnectedForeachInstructionInspector inspector = new DisconnectedForeachInstructionInspector();
        inspector.SUGGEST_USING_CLONE                           = true;
        myFixture.configureByFile("testData/fixtures/foreach/disconnected-statements-foreach-false-positives.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}

