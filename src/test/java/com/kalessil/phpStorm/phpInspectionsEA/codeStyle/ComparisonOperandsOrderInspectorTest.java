package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ComparisonOperandsOrderInspector;

public class ComparisonOperandsOrderInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsYodaPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        inspector.CONFIGURED                       = true;
        inspector.PREFER_YODA_STYLE                = true;

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-yoda.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsRegularPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        inspector.CONFIGURED                       = true;
        inspector.PREFER_REGULAR_STYLE             = true;

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-regular.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}