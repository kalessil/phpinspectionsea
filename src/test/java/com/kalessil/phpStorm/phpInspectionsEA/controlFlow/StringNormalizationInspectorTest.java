package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringNormalizationInspector;

final public class StringNormalizationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StringNormalizationInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/string-normalization.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/controlFlow/string-normalization.fixed.php");
    }
}
