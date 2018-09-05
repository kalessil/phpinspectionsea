package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.CallableParameterUseCaseInTypeContextInspection;

final public class CallableParameterUseCaseInTypeContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsNullPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.null.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsBooleanPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.boolean.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsArrayPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.array.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsStringPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.string.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsNumberPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.number.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsInstanceofPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.instanceof.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/types/parameter-types-checks.instanceof.fixed.php");
    }
}
