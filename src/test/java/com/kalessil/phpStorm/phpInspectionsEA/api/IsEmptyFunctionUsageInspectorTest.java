package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;

final public class IsEmptyFunctionUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        IsEmptyFunctionUsageInspector inspector = new IsEmptyFunctionUsageInspector();
        inspector.SUGGEST_TO_USE_COUNT_CHECK = true;
        inspector.REPORT_EMPTY_USAGE = true;
        inspector.SUGGEST_TO_USE_NULL_COMPARISON = true;

        myFixture.configureByFile("fixtures/api/empty-function.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
