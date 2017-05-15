package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ParameterEqualsDefaultValueInspector;

public final class ParameterEqualsDefaultValueInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/parameters-default-equals.php");
        myFixture.enableInspections(ParameterEqualsDefaultValueInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/codeStyle/parameters-default-equals.fixed.php");
    }
}
