package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ComparisonOperandsOrderInspector;

public final class ComparisonOperandsOrderInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsYodaPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        inspector.PREFER_YODA_STYLE                = true;

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-yoda.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsRegularPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        inspector.PREFER_REGULAR_STYLE             = true;

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-regular.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
