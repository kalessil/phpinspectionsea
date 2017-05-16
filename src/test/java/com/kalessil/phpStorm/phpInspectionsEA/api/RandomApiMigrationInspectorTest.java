package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.RandomApiMigrationInspector;

final public class RandomApiMigrationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsMtPatterns() {
        RandomApiMigrationInspector inspector = new RandomApiMigrationInspector();
        inspector.SUGGEST_USING_RANDOM_INT = false;

        myFixture.configureByFile("fixtures/api/random-api-mt.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsEdgePatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/api/random-api-edge.php");
        myFixture.enableInspections(RandomApiMigrationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

}
