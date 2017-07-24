package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrUsedAsStrPosInspector;

final public class SubStrUsedAsStrPosInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SubStrUsedAsStrPosInspector());

        myFixture.configureByFile("fixtures/controlFlow/substr-used-as-strpos.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/substr-used-as-strpos.fixed.php");
    }
}

