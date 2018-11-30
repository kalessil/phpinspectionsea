package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.SuspiciousBinaryOperationInspector;

final public class SuspiciousBinaryOperationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/pitfalls/suspicious-binary-operations.fixed.php");
    }
    public void testIfFindsMistypedOperations() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.mistyped.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsCountChecks() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.count.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsInvalidArrayOperationPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.array-concatenation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
