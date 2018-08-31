package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.InvertedIfElseConstructsInspector;

final public class InvertedIfElseConstructsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new InvertedIfElseConstructsInspector());
        myFixture.configureByFile("testData/fixtures/ifs/if-inverted-condition-else-normalization.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/ifs/if-inverted-condition-else-normalization.fixed.php");
    }
}

