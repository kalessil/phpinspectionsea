package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsCountableCanBeUsedInspector;

final public class IsCountableCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsAllPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.4");
        if (level != null && level.getVersionString().equals("7.4")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            myFixture.enableInspections(new IsCountableCanBeUsedInspector());
            myFixture.configureByFile("testData/fixtures/api/is-countable.php");
            myFixture.testHighlighting(true, false, true);
        }
    }
}
