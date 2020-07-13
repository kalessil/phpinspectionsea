package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis.ternaries;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ternaries.SuspiciousTernaryOperatorInspector;

final public class SuspiciousTernaryOperatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new SuspiciousTernaryOperatorInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/ternaries/suspicious-ternary-operator.php");
        myFixture.testHighlighting(true, false, true);
    }
}
