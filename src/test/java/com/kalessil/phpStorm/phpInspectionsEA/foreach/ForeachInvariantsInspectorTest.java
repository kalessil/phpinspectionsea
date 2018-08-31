package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachInvariantsInspector;

final public class ForeachInvariantsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ForeachInvariantsInspector());
        myFixture.configureByFile("testData/fixtures/foreach/foreach-invariants.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/foreach/foreach-invariants.fixed.php");
    }
}

