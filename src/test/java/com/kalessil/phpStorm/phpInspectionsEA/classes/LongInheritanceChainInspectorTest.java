package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.LongInheritanceChainInspector;

final public class LongInheritanceChainInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new LongInheritanceChainInspector());
        myFixture.configureByFile("testData/fixtures/classes/long-inheritance-chain.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new LongInheritanceChainInspector());
        myFixture.configureByFile("testData/fixtures/classes/long-inheritance-chain-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
