package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.NullPointerExceptionInspector;

final public class NullPointerExceptionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new NullPointerExceptionInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/npe.php");
        myFixture.testHighlighting(true, false, true);
    }
}
