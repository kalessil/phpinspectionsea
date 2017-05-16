package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.AdditionOperationOnArraysInspection;

final public class AdditionOperationOnArraysInspectionTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new AdditionOperationOnArraysInspection());

        myFixture.configureByFile("fixtures/lang/arrays-concatenate.php");
        myFixture.testHighlighting(true, false, true);
    }
}
