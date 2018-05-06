package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.CompositionAndInheritanceInspector;

final public class CompositionAndInheritanceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsGenericPatterns() {
        myFixture.enableInspections(new CompositionAndInheritanceInspector());
        myFixture.configureByFile("fixtures/classes/composition-inheritance.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsForcedPatterns() {
        final CompositionAndInheritanceInspector inspector = new CompositionAndInheritanceInspector();
        inspector.FORCE_FINAL_OR_ABSTRACT                  = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/classes/composition-inheritance-force.php");
        myFixture.testHighlighting(true, false, true);
    }
}
