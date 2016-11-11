package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.EmptyClassInspector;

public class EmptyClassInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/empty-class.php");
        myFixture.enableInspections(EmptyClassInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/empty-class-false-positives.php");
        myFixture.enableInspections(EmptyClassInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

