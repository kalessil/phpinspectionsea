package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

final public class IsNullFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsAndWithInYodaStyle() {
        ComparisonStyle.force(ComparisonStyle.YODA);

        myFixture.enableInspections(new IsNullFunctionUsageInspector());
        myFixture.configureByFile("fixtures/api/is-null-function.yoda.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.yoda.fixed.php");

        ComparisonStyle.force(ComparisonStyle.REGULAR);
    }
    public void testIfFindsAllPatternsAndWithInRegularStyle() {
        myFixture.enableInspections(new IsNullFunctionUsageInspector());
        myFixture.configureByFile("fixtures/api/is-null-function.regular.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/is-null-function.regular.fixed.php");
    }
}