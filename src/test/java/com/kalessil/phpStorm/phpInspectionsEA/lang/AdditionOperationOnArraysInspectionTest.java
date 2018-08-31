package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.AdditionOperationOnArraysInspection;

final public class AdditionOperationOnArraysInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new AdditionOperationOnArraysInspection());
        myFixture.configureByFile("testData/fixtures/lang/arrays-concatenate.php");
        myFixture.testHighlighting(true, false, true);
    }
}
