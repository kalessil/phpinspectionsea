package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

final public class TypeUnsafeComparisonInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/types/type-unsafe-comparison.php");
        myFixture.enableInspections(TypeUnsafeComparisonInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/types/type-unsafe-comparison.fixed.php");
    }
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/types/type-unsafe-comparison-false-positives.php");
        myFixture.enableInspections(TypeUnsafeComparisonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
