package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachInvariantsInspector;

final public class ForeachInvariantsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/foreach/foreach-invariants.php");
        myFixture.enableInspections(ForeachInvariantsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/foreach-invariants-false-positives.php");
        myFixture.enableInspections(ForeachInvariantsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

