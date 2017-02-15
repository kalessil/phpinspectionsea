package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.LongInheritanceChainInspector;

final public class LongInheritanceChainInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/long-inheritance-chain.php");
        myFixture.enableInspections(LongInheritanceChainInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/long-inheritance-chain-false-positives.php");
        myFixture.enableInspections(LongInheritanceChainInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
