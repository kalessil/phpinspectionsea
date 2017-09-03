package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

final public class IsNullFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final IsNullFunctionUsageInspector inspector = new IsNullFunctionUsageInspector();
        inspector.PREFER_YODA_STYLE    = true;
        inspector.PREFER_REGULAR_STYLE = false;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/api/is-null-function.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.fixed.php");
    }
}