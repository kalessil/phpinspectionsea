package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
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
    public void testIfFindsPossiblyTyposPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.possibly-typos.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsNullCoalescingPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.null-coalescing.php");
        myFixture.testHighlighting(true, false, true);
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
    public void testIfFindsIsNumericChecks() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.is_numeric.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsVersionChecks() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP560);
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.version.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsInvalidArrayOperationPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.array-concatenation.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsComparingNonIntersectingFunctionResultsPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.non-intersecting-types.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsMultipleFalsyValuesCheckPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.multiple-falsy-values.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsSimplifyBooleansComparisonPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-binary-operations.simplify-boolean-comparison.php");
        myFixture.testHighlighting(true, false, true);
    }
}
