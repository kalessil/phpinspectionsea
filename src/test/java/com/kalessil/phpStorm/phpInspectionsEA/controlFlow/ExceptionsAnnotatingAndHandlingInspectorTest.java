package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.ExceptionsAnnotatingAndHandlingInspector;

final public class ExceptionsAnnotatingAndHandlingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testAnnotationsProcessing() {
        myFixture.enableInspections(new ExceptionsAnnotatingAndHandlingInspector());
        myFixture.configureByFile("fixtures/controlFlow/exceptions-workflow.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/controlFlow/exceptions-workflow.fixed.php");
    }
}
