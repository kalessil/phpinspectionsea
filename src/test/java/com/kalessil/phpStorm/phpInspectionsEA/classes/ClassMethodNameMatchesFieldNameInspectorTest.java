package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassMethodNameMatchesFieldNameInspector;

final public class ClassMethodNameMatchesFieldNameInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/class-field-method-named-identically.php");
        myFixture.enableInspections(ClassMethodNameMatchesFieldNameInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}