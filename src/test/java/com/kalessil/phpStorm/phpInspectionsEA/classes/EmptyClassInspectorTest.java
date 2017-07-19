package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.EmptyClassInspector;

final public class EmptyClassInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new EmptyClassInspector());

        myFixture.configureByFile("fixtures/classes/empty-class.php");
        myFixture.testHighlighting(true, false, true);
    }
}

