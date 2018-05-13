package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.MissingIssetImplementationInspector;

final public class MissingIssetImplementationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new MissingIssetImplementationInspector());
        myFixture.configureByFile("fixtures/classes/empty-isset-results-correctness.php");
        myFixture.testHighlighting(true, false, true);
    }
}
