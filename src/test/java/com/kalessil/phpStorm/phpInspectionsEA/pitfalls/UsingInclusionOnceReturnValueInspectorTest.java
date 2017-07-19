package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UsingInclusionOnceReturnValueInspector;

final public class UsingInclusionOnceReturnValueInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/using-inclusion-once-return.php");
        myFixture.enableInspections(UsingInclusionOnceReturnValueInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}