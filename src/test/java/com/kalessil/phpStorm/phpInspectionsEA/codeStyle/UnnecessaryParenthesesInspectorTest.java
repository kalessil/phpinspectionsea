package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UnnecessaryParenthesesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class UnnecessaryParenthesesInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/unnecessary-parentheses.php");
        myFixture.enableInspections(UnnecessaryParenthesesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
