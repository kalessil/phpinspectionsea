package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

final public class IsNullFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsAndWithInYodaStyle() {
        final IsNullFunctionUsageInspector inspector = new IsNullFunctionUsageInspector();
        inspector.PREFER_YODA_STYLE    = true;
        inspector.PREFER_REGULAR_STYLE = false;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/api/is-null-function.yoda.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.yoda.fixed.php");
    }
    public void testIfFindsAllPatternsAndWithInRegularStyle() {
        final IsNullFunctionUsageInspector inspector = new IsNullFunctionUsageInspector();
        inspector.PREFER_YODA_STYLE    = false;
        inspector.PREFER_REGULAR_STYLE = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/api/is-null-function.regular.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.regular.fixed.php");
    }
}