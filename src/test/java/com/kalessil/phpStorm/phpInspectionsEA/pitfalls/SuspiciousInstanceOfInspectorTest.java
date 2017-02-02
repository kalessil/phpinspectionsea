package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.SuspiciousInstanceOfInspector;

final public class SuspiciousInstanceOfInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/instanceof-against-traits.php");
        myFixture.enableInspections(SuspiciousInstanceOfInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
