package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MissingOrEmptyGroupStatementInspector;

final public class MissingOrEmptyGroupStatementInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsMissingStatements() {
        myFixture.configureByFile("fixtures/codeStyle/group-statements-missing.php");
        myFixture.enableInspections(MissingOrEmptyGroupStatementInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsEmptyStatements() {
        myFixture.configureByFile("fixtures/codeStyle/group-statements-empty.php");
        myFixture.enableInspections(MissingOrEmptyGroupStatementInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
