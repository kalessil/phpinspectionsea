package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousReturnInspector;

final public class SuspiciousReturnInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousReturnInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/return-in-finally.php");
        myFixture.testHighlighting(true, false, true);
    }
}
