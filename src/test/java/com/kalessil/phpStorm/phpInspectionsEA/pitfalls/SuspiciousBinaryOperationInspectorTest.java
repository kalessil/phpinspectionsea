package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.SuspiciousBinaryOperationInspector;

final public class SuspiciousBinaryOperationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousBinaryOperationInspector());
        myFixture.configureByFile("fixtures/pitfalls/suspicious-binary-operations.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/pitfalls/suspicious-binary-operations.fixed.php");
    }
}
