package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.NullPointerExceptionInspector;

final public class NullPointerExceptionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new NullPointerExceptionInspector());

        myFixture.configureByFile("fixtures/pitfalls/npe.php");
        myFixture.testHighlighting(true, false, true);
    }
}
