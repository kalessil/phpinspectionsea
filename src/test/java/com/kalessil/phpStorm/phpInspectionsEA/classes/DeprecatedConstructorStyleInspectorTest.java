package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.DeprecatedConstructorStyleInspector;

final public class DeprecatedConstructorStyleInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/deprecated-constructors.php");
        myFixture.enableInspections(DeprecatedConstructorStyleInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
