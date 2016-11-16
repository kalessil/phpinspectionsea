package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.SwitchContinuationInLoopInspector;

final public class SwitchContinuationInLoopInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/continue-in-switch.php");
        myFixture.enableInspections(SwitchContinuationInLoopInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}