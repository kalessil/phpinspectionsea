package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;

final public class IsEmptyFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final IsEmptyFunctionUsageInspector inspector = new IsEmptyFunctionUsageInspector();
        inspector.SUGGEST_TO_USE_COUNT_CHECK          = true;
        inspector.REPORT_EMPTY_USAGE                  = true;
        inspector.SUGGEST_TO_USE_NULL_COMPARISON      = true;
        inspector.PREFER_REGULAR_STYLE                = false;
        inspector.PREFER_YODA_STYLE                   = true;
        myFixture.configureByFile("fixtures/api/empty-function.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/empty-function.fixed.php");
    }
}
