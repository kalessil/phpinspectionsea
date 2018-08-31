package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MisorderedModifiersInspector;

final public class MisorderedModifiersInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MisorderedModifiersInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/misordered-modifiers.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/misordered-modifiers.fixed.php");
    }
}
