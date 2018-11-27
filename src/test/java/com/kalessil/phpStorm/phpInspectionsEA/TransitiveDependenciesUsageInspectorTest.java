package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.TransitiveDependenciesUsageInspector;

final public class TransitiveDependenciesUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatternsWithManifest() {
        final TransitiveDependenciesUsageInspector inspection = new TransitiveDependenciesUsageInspector();
        inspection.configuration.add("vendor/ignored");

        myFixture.enableInspections(inspection);
        myFixture.configureByFiles(
                "testData/fixtures/transitiveDependencies/application/Application.php",
                "testData/fixtures/transitiveDependencies/application/composer.json",
                "testData/fixtures/transitiveDependencies/Bootstrap.php",
                "testData/fixtures/transitiveDependencies/application/vendor/first/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/first/composer.json",
                "testData/fixtures/transitiveDependencies/application/vendor/second/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/second/composer.json",
                "testData/fixtures/transitiveDependencies/application/vendor/ignored/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/ignored/composer.json"
        );
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsPatternsWithoutManifest() {
        final TransitiveDependenciesUsageInspector inspection = new TransitiveDependenciesUsageInspector();
        inspection.configuration.add("vendor/ignored");

        myFixture.enableInspections(inspection);
        myFixture.configureByFiles(
                "testData/fixtures/transitiveDependencies/Bootstrap.php",
                "testData/fixtures/transitiveDependencies/application/composer.json",
                "testData/fixtures/transitiveDependencies/application/vendor/first/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/first/composer.json",
                "testData/fixtures/transitiveDependencies/application/vendor/second/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/second/composer.json",
                "testData/fixtures/transitiveDependencies/application/vendor/ignored/Clazz.php",
                "testData/fixtures/transitiveDependencies/application/vendor/ignored/composer.json"
        );
        myFixture.testHighlighting(true, false, true);
    }
}
