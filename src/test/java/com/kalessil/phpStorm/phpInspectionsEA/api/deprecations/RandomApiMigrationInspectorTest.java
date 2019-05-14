package com.kalessil.phpStorm.phpInspectionsEA.api.deprecations;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.deprecations.RandomApiMigrationInspector;

final public class RandomApiMigrationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsMtPatterns() {
        RandomApiMigrationInspector inspector = new RandomApiMigrationInspector();
        inspector.SUGGEST_USING_RANDOM_INT    = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/deprecations/random-api-mt.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/deprecations/random-api-mt.fixed.php");
    }

    public void testIfFindsEdgePatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new RandomApiMigrationInspector());
        myFixture.configureByFile("testData/fixtures/api/deprecations/random-api-edge.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/deprecations/random-api-edge.fixed.php");
    }
}
