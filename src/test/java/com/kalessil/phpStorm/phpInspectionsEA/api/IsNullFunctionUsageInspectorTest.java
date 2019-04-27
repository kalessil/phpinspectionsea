package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateProjectSettings;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

final public class IsNullFunctionUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsAndWithInYodaStyle() {
        myFixture.getProject().getComponent(EAUltimateProjectSettings.class).setPreferringYodaComparisonStyle(true);

        myFixture.enableInspections(new IsNullFunctionUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/is-null-function.yoda.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/is-null-function.yoda.fixed.php");

        myFixture.getProject().getComponent(EAUltimateProjectSettings.class).setPreferringYodaComparisonStyle(false);
    }
    public void testIfFindsAllPatternsAndWithInRegularStyle() {
        myFixture.enableInspections(new IsNullFunctionUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/is-null-function.regular.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/is-null-function.regular.fixed.php");
    }
}
