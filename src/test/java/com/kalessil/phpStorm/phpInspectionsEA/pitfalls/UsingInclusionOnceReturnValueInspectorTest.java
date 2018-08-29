package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UsingInclusionOnceReturnValueInspector;

final public class UsingInclusionOnceReturnValueInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UsingInclusionOnceReturnValueInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/using-inclusion-once-return.php");
        myFixture.testHighlighting(true, false, true);
    }
}
