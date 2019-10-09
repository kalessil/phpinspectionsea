package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.loops.MissingLoopTerminationInspector;

final public class MissingLoopTerminationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MissingLoopTerminationInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/missing-loop-termination.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/semanticalAnalysis/missing-loop-termination.fixed.php");
    }
}
