package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.SimpleXmlLoadFileUsageInspector;

final public class SimpleXmlLoadFileUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SimpleXmlLoadFileUsageInspector());
        myFixture.configureByFile("fixtures/api/simplexml_load_file-usage.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/simplexml_load_file-usage.fixed.php");
    }
}