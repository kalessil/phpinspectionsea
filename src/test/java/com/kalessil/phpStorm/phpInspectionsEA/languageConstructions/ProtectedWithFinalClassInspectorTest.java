package com.kalessil.phpStorm.phpInspectionsEA.languageConstructions;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ProtectedWithFinalClassInspector;

public class ProtectedWithFinalClassInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeSmell/protected-with-final-class.php");
        myFixture.enableInspections(ProtectedWithFinalClassInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
