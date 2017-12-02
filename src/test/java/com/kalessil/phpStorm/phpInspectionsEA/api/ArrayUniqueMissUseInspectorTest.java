package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayUniqueMissUseInspector;

final public class ArrayUniqueMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArrayUniqueMissUseInspector());
        myFixture.configureByFile("fixtures/api/array/array-unique-misused.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/array/array-unique-misused.fixed.php");
    }
}
