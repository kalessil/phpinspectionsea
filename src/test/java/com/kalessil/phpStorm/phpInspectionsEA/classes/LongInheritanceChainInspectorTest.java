package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.LongInheritanceChainInspector;

final public class LongInheritanceChainInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final LongInheritanceChainInspector inspector = new LongInheritanceChainInspector();
        inspector.COMPLAIN_THRESHOLD = 3;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/classes/long-inheritance-chain.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new LongInheritanceChainInspector());
        myFixture.configureByFile("testData/fixtures/classes/long-inheritance-chain-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
