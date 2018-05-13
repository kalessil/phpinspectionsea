package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.CallableMethodValidityInspector;

final public class CallableMethodValidityInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new CallableMethodValidityInspector());
        myFixture.configureByFile("fixtures/classes/callable-methods-validity.php");
        myFixture.configureByFile("fixtures/classes/callable-exceptions-handler-validity.php");
        myFixture.testHighlighting(true, false, true);
    }
}
