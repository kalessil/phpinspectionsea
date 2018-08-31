package com.kalessil.phpStorm.phpInspectionsEA.api.deprecations;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations.DynamicCallsToScopeIntrospectionInspector;

final public class DynamicCallsToScopeIntrospectionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new DynamicCallsToScopeIntrospectionInspector());
        myFixture.configureByFile("testData/fixtures/api/deprecations/dynamic-calls-to-scope-introspection.php");
        myFixture.testHighlighting(true, false, true);
    }
}
