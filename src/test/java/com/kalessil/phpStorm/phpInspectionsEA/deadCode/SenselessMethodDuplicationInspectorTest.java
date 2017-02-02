package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SenselessMethodDuplicationInspector;

final public class SenselessMethodDuplicationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/senseless-method-duplication.php");
        myFixture.enableInspections(SenselessMethodDuplicationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/deadCode/senseless-method-duplication-false-positives.php");
        myFixture.enableInspections(SenselessMethodDuplicationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}