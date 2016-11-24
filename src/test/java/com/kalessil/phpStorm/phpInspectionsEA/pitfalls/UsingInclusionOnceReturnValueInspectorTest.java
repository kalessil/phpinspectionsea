package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UsingInclusionOnceReturnValueInspector;

final public class UsingInclusionOnceReturnValueInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/using-inclusion-once-return.php");
        myFixture.enableInspections(UsingInclusionOnceReturnValueInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}