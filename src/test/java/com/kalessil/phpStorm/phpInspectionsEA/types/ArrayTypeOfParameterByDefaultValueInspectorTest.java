package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

public class ArrayTypeOfParameterByDefaultValueInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/types/type-can-be-array.php");
        myFixture.enableInspections(ArrayTypeOfParameterByDefaultValueInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

