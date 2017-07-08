package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference.ReferenceMismatchInspector;

final public class ReferenceMismatchInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ReferenceMismatchInspector());

        myFixture.configureByFile("fixtures/reference-mismatch-foreach.php");
        myFixture.testHighlighting(true, false, true);
    }
}