package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.AvoidNotConditionalsInspector;

public final class AvoidNotConditionalsInspectorTest extends CodeInsightFixtureTestCase {
    public void testThatWeCanAvoidNotOperatorOnIfs() {
        myFixture.configureByFile("fixtures/ifs/if-avoid-not-operator.php");
        myFixture.enableInspections(AvoidNotConditionalsInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/ifs/if-avoid-not-operator.fixed.php");
    }
}

