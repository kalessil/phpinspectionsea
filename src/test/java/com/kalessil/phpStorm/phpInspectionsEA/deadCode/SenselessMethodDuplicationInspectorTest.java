package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.SenselessMethodDuplicationInspector;

final public class SenselessMethodDuplicationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        SenselessMethodDuplicationInspector inspector = new SenselessMethodDuplicationInspector();
        inspector.MAX_METHOD_SIZE                     = 20;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/deadCode/senseless-method-duplication.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/deadCode/senseless-method-duplication.fixed.php");
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new SenselessMethodDuplicationInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/senseless-method-duplication-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
