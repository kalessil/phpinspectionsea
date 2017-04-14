package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.npe.NullPointerExceptionInspector;

final public class NullPointerExceptionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(NullPointerExceptionInspector.class);

        myFixture.configureByFile("fixtures/pitfalls/npe.php");
        myFixture.testHighlighting(true, false, true);
    }
}
