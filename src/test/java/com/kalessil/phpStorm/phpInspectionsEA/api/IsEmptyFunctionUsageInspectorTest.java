package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;

final public class IsEmptyFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final IsEmptyFunctionUsageInspector inspector = new IsEmptyFunctionUsageInspector();
        inspector.SUGGEST_TO_USE_COUNT_CHECK          = true;
        inspector.REPORT_EMPTY_USAGE                  = true;
        inspector.SUGGEST_TO_USE_NULL_COMPARISON      = true;
        inspector.SUGGEST_NULL_COMPARISON_FOR_SCALARS = true;
        myFixture.configureByFile("testData/fixtures/api/empty-function.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/empty-function.fixed.php");
    }
}
