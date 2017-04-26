package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.CallableParameterUseCaseInTypeContextInspection;

final public class CallableParameterUseCaseInTypeContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(CallableParameterUseCaseInTypeContextInspection.class);

        myFixture.configureByFile("fixtures/types/parameter-types-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
