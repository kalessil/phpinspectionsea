package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.IfReturnReturnSimplificationInspector;

final public class IfReturnReturnSimplificationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new IfReturnReturnSimplificationInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/if-return-return-simplify.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/if-return-return-simplify.fixed.php");
    }
}
