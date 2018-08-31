package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.OpAssignShortSyntaxInspector;

final public class OpAssignShortSyntaxInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new OpAssignShortSyntaxInspector());
        myFixture.configureByFile("testData/fixtures/lang/op-assign-short-syntax.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/op-assign-short-syntax.fixed.php");
    }
}
