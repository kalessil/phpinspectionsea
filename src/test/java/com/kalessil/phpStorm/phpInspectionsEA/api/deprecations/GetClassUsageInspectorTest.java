package com.kalessil.phpStorm.phpInspectionsEA.api.deprecations;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations.GetClassUsageInspector;

final public class GetClassUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new GetClassUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/deprecations/get-class-with-null.php");
        myFixture.testHighlighting(true, false, true);
    }
}
