package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrUsedAsStrPosInspector;

public final class SubStrUsedAsStrPosInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final SubStrUsedAsStrPosInspector inspector = new SubStrUsedAsStrPosInspector();
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/controlFlow/substr-used-as-strpos.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/substr-used-as-strpos.fixed.php");
    }
}

