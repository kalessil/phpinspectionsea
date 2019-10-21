package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.CallableParameterUseCaseInTypeContextInspection;

final public class CallableParameterUseCaseInTypeContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableParameterUseCaseInTypeContextInspection());
        myFixture.configureByFile("testData/fixtures/types/parameter-types-checks.php");
        myFixture.testHighlighting(true, false, true);
    }
}
