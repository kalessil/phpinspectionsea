package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.byReference.ParameterByRefWithDefaultInspector;

final public class ParameterByRefWithDefaultInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/parameter-ref-with-default.php");
        myFixture.enableInspections(ParameterByRefWithDefaultInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
