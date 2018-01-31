package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.CallableParameterUseCaseInTypeContextInspection;

final public class CallableParameterUseCaseInTypeContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("fixtures/types/parameter-types-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsNullPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("fixtures/types/parameter-types-checks.null.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsInstanceofPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("fixtures/types/parameter-types-checks.instanceof.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/types/parameter-types-checks.instanceof.fixed.php");
    }
}
