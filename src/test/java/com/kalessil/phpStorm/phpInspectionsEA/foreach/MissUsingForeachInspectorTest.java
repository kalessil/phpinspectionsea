package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.MissUsingForeachInspector;

final public class MissUsingForeachInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new MissUsingForeachInspector());
        myFixture.configureByFile("testData/fixtures/foreach/misused-foreach.php");
        myFixture.testHighlighting(true, false, true);
    }
}