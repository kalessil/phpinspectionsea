package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NestedPositiveIfStatementsInspector;

final public class NestedPositiveIfStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindsAllPatterns() {
        myFixture.enableInspections(new NestedPositiveIfStatementsInspector());
        myFixture.configureByFile("testData/fixtures/ifs/nested-positive-ifs.php");
        myFixture.testHighlighting(true, false, true);

        try {
            myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
            myFixture.setTestDataPath(".");
            myFixture.checkResultByFile("testData/fixtures/ifs/nested-positive-ifs.fixed.php");
        } catch (final IllegalStateException failure) {
            /* PS 2016.* sometimes throw this except and breaking th build */
            if (!failure.getMessage().equals("Attempt to modify PSI for non-committed Document!")) {
                throw failure;
            }
        }
    }
}
