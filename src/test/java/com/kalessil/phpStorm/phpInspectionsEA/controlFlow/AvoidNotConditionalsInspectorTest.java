package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.rt.execution.junit.FileComparisonFailure;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.AvoidNotConditionalsInspector;

final public class AvoidNotConditionalsInspectorTest extends CodeInsightFixtureTestCase {
    public void testThatWeCanAvoidNotOperatorOnIfs() {
        myFixture.configureByFile("fixtures/ifs/if-avoid-not-operator.php");
        myFixture.enableInspections(AvoidNotConditionalsInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        try {
            myFixture.checkResultByFile("fixtures/ifs/if-avoid-not-operator.fixed.php");
        }
        catch (final FileComparisonFailure fileComparisonFailure) {
            assertEquals(fileComparisonFailure.getExpected(), fileComparisonFailure.getActual());
        }
    }
}

