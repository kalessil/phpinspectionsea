package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.MissingIssetImplementationInspector;

final public class MissingIssetImplementationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new MissingIssetImplementationInspector());

        myFixture.configureByFile("fixtures/classes/empty-isset-results-correctness.php");
        myFixture.testHighlighting(true, false, true);
    }
}
