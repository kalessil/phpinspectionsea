package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnusedGotoLabelInspector;

final public class UnusedGotoLabelInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/unused-goto.php");
        myFixture.enableInspections(UnusedGotoLabelInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
