package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryFinalModifierInspector;

public class UnnecessaryFinalModifierInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unnecessary-final-modifier.php");
        myFixture.enableInspections(UnnecessaryFinalModifierInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
