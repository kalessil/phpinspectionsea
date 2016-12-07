package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ClassOverridesFieldOfSuperClassInspector;

final public class ClassOverridesFieldOfSuperClassInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/class-overrides-fields.php");
        myFixture.enableInspections(ClassOverridesFieldOfSuperClassInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}