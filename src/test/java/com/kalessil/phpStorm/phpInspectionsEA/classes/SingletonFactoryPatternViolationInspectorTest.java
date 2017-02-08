package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SingletonFactoryPatternViolationInspector;

final public class SingletonFactoryPatternViolationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIncorrectSingletonConstructorVisibility () {
        myFixture.configureByFile("fixtures/designPatterns/singleton-constructor-must-be-non-public.php");
        myFixture.enableInspections(SingletonFactoryPatternViolationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFactoryPatterns () {
        myFixture.configureByFile("fixtures/designPatterns/factory-needs-factory-methods.php");
        myFixture.enableInspections(SingletonFactoryPatternViolationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/singleton-factory-false-positives.php");
        myFixture.enableInspections(SingletonFactoryPatternViolationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
