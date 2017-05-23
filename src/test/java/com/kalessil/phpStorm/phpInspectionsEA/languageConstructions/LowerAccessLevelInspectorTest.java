package com.kalessil.phpStorm.phpInspectionsEA.languageConstructions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.LowerAccessLevelInspector;

public class LowerAccessLevelInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new LowerAccessLevelInspector());

        myFixture.configureByFile("fixtures/codeSmell/protected-with-final-class.php");
        myFixture.testHighlighting(true, false, true);

        for (final IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/codeSmell/protected-with-final-class.fixed.php");
    }
}
