package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DispatchingThisIntoClosuresInspector;

public class DispatchingThisIntoClosuresInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/dispatching-this-into-closures.php");
        myFixture.enableInspections(DispatchingThisIntoClosuresInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

