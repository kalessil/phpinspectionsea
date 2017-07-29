package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

final public class TypeUnsafeComparisonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/types/type-unsafe-comparison.php");
        myFixture.enableInspections(TypeUnsafeComparisonInspector.class);
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
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
