package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.ForeachInvariantsInspector;

public class ForeachInvariantsInspectorTest extends CodeInsightFixtureTestCase {
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

