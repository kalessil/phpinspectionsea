package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MissingOrEmptyGroupStatementInspector;

final public class MissingOrEmptyGroupStatementInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsMissingStatements() {
        myFixture.enableInspections(new MissingOrEmptyGroupStatementInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/group-statements-missing.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/group-statements-missing.fixed.php");
    }
    public void testIfFindsEmptyStatements() {
        final MissingOrEmptyGroupStatementInspector inspector = new MissingOrEmptyGroupStatementInspector();
        inspector.REPORT_EMPTY_BODY                           = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/group-statements-empty.php");
        myFixture.testHighlighting(true, false, true);
    }
}
