package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnusedGotoLabelInspector;

final public class UnusedGotoLabelInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/unused-goto.php");
        myFixture.enableInspections(UnusedGotoLabelInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
