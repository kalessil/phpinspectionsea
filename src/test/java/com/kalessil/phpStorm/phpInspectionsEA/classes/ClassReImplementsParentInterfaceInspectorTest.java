package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ClassReImplementsParentInterfaceInspector;

final public class ClassReImplementsParentInterfaceInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/re-implements-parent-interface.php");
        myFixture.enableInspections(ClassReImplementsParentInterfaceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

