package com.kalessil.phpStorm.phpInspectionsEA;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.TransitiveDependenciesUsageInspector;

final public class TransitiveDependenciesUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testNoComposerJson() {
        myFixture.enableInspections(new TransitiveDependenciesUsageInspector());
        myFixture.configureByFiles(
                "fixtures/transitiveDependencies/application/vendor/first/Clazz.php",
                "fixtures/transitiveDependencies/application/vendor/first/composer.json",
                "fixtures/transitiveDependencies/application/vendor/second/Clazz.php",
                "fixtures/transitiveDependencies/application/vendor/second/composer.json",
                "fixtures/transitiveDependencies/application/Application.php",
                "fixtures/transitiveDependencies/application/composer.json",
                "fixtures/transitiveDependencies/Bootstrap.php"
        );
        myFixture.testHighlighting(true, false, true);
    }
}
