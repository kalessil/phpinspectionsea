package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryElseInspector;

final public class UnnecessaryElseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/unnecessary-else-elseif.php");
        myFixture.enableInspections(UnnecessaryElseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}