package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.PropertyCanBeStaticInspector;

final public class PropertyCanBeStaticInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP550);
        myFixture.enableInspections(new PropertyCanBeStaticInspector());
        myFixture.configureByFile("testData/fixtures/classes/property-can-be-static.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new PropertyCanBeStaticInspector());
        myFixture.configureByFile("testData/fixtures/classes/property-can-be-static-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
