package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IncrementDecrementOperationEquivalentInspector;

final public class IncrementDecrementOperationEquivalentInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        IncrementDecrementOperationEquivalentInspector fixer = new IncrementDecrementOperationEquivalentInspector();
        fixer.PREFER_SUFFIX_STYLE = true;
        fixer.PREFER_PREFIX_STYLE = false;

        myFixture.enableInspections(fixer);

        myFixture.configureByFile("testData/fixtures/codeStyle/increment-decrement-can-be-used.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/increment-decrement-can-be-used.fixed.php");
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new IncrementDecrementOperationEquivalentInspector());

        myFixture.configureByFile("testData/fixtures/codeStyle/increment-decrement-can-be-used-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
