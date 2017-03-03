package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryElseInspector;

/**
 * (c) Funivan <alotofall@gmail.com>
 */
public class UnnecessaryElseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/unnecessary-else.php");
        myFixture.enableInspections(new UnnecessaryElseInspector());
        myFixture.testHighlighting(true, false, true);
    }

}