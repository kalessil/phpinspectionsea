package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MultipleReturnPointsInspector;

public class MultipleReturnPointsInspectorTest extends CodeInsightFixtureTestCase {

    public void testMultipleReturnPoints() {
        myFixture.configureByFile("fixtures/codeStyle/multiple-return-points.php");
        myFixture.enableInspections(MultipleReturnPointsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

}
