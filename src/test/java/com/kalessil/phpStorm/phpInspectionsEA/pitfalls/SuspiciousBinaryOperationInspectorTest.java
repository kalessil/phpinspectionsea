package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.SuspiciousBinaryOperationInspector;

final public class SuspiciousBinaryOperationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final SuspiciousBinaryOperationInspector inspector = new SuspiciousBinaryOperationInspector();
        inspector.VERIFY_UNCLEAR_OPERATIONS_PRIORITIES     = true;
        inspector.VERIFY_CONSTANTS_IN_CONDITIONS           = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/pitfalls/suspicious-binary-operations.fixed.php");
    }
    public void testIfFindsArrayConcatenationPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.array-concatenation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
