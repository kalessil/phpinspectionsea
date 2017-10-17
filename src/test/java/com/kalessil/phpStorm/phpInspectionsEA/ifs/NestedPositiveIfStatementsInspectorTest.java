package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NestedPositiveIfStatementsInspector;

final public class NestedPositiveIfStatementsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindsAllPatterns() {
        myFixture.enableInspections(new NestedPositiveIfStatementsInspector());
        myFixture.configureByFile("fixtures/ifs/nested-positive-ifs.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/ifs/nested-positive-ifs.fixed.php");
    }
}
