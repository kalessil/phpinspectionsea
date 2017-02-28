package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.UnusedConstructorDependenciesInspector;

final public class UnusedConstructorDependenciesInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/unused-constructor-dependencies.php");
        myFixture.enableInspections(UnusedConstructorDependenciesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
