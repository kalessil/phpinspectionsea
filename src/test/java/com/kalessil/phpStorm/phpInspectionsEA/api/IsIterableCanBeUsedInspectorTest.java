package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsIterableCanBeUsedInspector;

final public class IsIterableCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new IsIterableCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/is-iterable.php");
        myFixture.testHighlighting(true, false, true);
    }
}
