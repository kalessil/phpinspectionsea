package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryUseAliasInspector;

public class UnnecessaryUseAliasInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/use-aliases.php");
        myFixture.enableInspections(UnnecessaryUseAliasInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
