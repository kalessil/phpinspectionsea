package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.AdditionOperationOnArraysInspection;

public class AdditionOperationOnArraysInspectionTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/arrays-concatenate.php");
        myFixture.enableInspections(AdditionOperationOnArraysInspection.class);
        myFixture.testHighlighting(true, false, true);
    }
}
