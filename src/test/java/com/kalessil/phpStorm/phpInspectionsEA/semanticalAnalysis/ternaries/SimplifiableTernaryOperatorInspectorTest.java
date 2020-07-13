package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis.ternaries;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ternaries.SimplifiableTernaryOperatorInspector;

final public class SimplifiableTernaryOperatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SimplifiableTernaryOperatorInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/ternaries/simplifiable-ternary-operator.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/semanticalAnalysis/ternaries/simplifiable-ternary-operator.fixed.php");
    }
}
