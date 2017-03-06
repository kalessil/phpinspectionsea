package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryElseInspector;


final public class UnnecessaryElseInspectorTest extends CodeInsightFixtureTestCase {

    public void testUnnecessaryElse() {
        myFixture.configureByFile("fixtures/lang/unnecessary-else.php");
        UnnecessaryElseInspector inspector = new UnnecessaryElseInspector();
        inspector.CHECK_ELSEIF = false;
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testUnnecessaryElseIf() {
        myFixture.configureByFile("fixtures/lang/unnecessary-elseif.php");
        UnnecessaryElseInspector inspector = new UnnecessaryElseInspector();
        inspector.CHECK_ELSEIF = true;
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

}