package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.TypeUnsafeArraySearchInspector;

final public class TypeUnsafeArraySearchInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns4() {
        myFixture.configureByFile("fixtures/api/strict-array-search.php");
        myFixture.enableInspections(TypeUnsafeArraySearchInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/strict-array-search.fixed.php");
    }
}
