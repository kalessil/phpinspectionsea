package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

final public class ArrayTypeOfParameterByDefaultValueInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(ArrayTypeOfParameterByDefaultValueInspector.class);

        myFixture.configureByFile("fixtures/types/type-can-be-array.php");
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/types/type-can-be-array.fixed.php");
    }
}