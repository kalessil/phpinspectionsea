package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

public final class IsNullFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsAndWithInYodaStyle() {
        final IsNullFunctionUsageInspector inspector = new IsNullFunctionUsageInspector();
        ComparisonStyle.setTemporarily(ComparisonStyle.YODA);

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/api/is-null-function.yoda.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.yoda.fixed.php");

        ComparisonStyle.setTemporarily(null);
    }

    public void testIfFindsAllPatternsAndWithInRegularStyle() {
        final IsNullFunctionUsageInspector inspector = new IsNullFunctionUsageInspector();
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/api/is-null-function.regular.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.regular.fixed.php");
    }
}
