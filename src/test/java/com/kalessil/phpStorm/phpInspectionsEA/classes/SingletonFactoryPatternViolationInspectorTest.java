package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SingletonFactoryPatternViolationInspector;

final public class SingletonFactoryPatternViolationInspectorTest extends CodeInsightFixtureTestCase {
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/singleton-factory-false-positives.php");
        myFixture.enableInspections(SingletonFactoryPatternViolationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
