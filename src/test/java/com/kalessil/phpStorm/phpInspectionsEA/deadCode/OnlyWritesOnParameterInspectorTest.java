package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OnlyWritesOnParameterInspector;

final public class OnlyWritesOnParameterInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/parameters-writes-only.php");
        myFixture.enableInspections(OnlyWritesOnParameterInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}