package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SideEffectAnalysisInspector;

final public class SideEffectAnalysisInspectionTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllSideEffects() {
        myFixture.configureByFile("fixtures/deadCode/side-effects.php");
        myFixture.enableInspections(SideEffectAnalysisInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
