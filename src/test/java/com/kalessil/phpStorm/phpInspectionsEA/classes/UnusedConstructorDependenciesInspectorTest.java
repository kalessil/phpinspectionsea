package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.UnusedConstructorDependenciesInspector;

final public class UnusedConstructorDependenciesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnusedConstructorDependenciesInspector());
        myFixture.configureByFile("testData/fixtures/classes/unused-constructor-dependencies.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testReporting() {
        myFixture.enableInspections(new UnusedConstructorDependenciesInspector());
        myFixture.configureByFile("testData/fixtures/classes/unused-constructor-dependencies.smart-reporting.php");
        myFixture.testHighlighting(true, false, true);
    }
}
