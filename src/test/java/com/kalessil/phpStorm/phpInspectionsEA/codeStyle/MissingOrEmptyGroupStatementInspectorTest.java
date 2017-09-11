package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MissingOrEmptyGroupStatementInspector;

final public class MissingOrEmptyGroupStatementInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsMissingStatements() {
        myFixture.enableInspections(new MissingOrEmptyGroupStatementInspector());
        myFixture.configureByFile("fixtures/codeStyle/group-statements-missing.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsEmptyStatements() {
        myFixture.enableInspections(new MissingOrEmptyGroupStatementInspector());
        myFixture.configureByFile("fixtures/codeStyle/group-statements-empty.php");
        myFixture.testHighlighting(true, false, true);
    }
}
