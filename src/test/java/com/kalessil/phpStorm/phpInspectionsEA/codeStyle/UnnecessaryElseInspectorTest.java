package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryElseInspector;

/**
 * Created by ivan on 01.03.17.
 */
public class UnnecessaryElseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unnecessary-else.php");
        myFixture.enableInspections(UnnecessaryElseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

}