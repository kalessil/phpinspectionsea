package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.InterfacesAsConstructorDependenciesInspector;

final public class InterfacesAsConstructorDependenciesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final InterfacesAsConstructorDependenciesInspector inspector = new InterfacesAsConstructorDependenciesInspector();
        inspector.TOLERATE_MISSING_CONTRACTS                         = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/classes/interfaces-as-constructor-dependencies.php");
        myFixture.testHighlighting(true, false, true);
    }
}
