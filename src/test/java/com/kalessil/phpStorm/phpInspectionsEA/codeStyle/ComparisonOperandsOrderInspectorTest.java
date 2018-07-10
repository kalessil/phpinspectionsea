package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ComparisonOperandsOrderInspector;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

public final class ComparisonOperandsOrderInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsYodaPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        ComparisonStyle.force(ComparisonStyle.YODA);

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-yoda.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        ComparisonStyle.force(ComparisonStyle.REGULAR);
    }

    public void testIfFindsRegularPatterns() {
        ComparisonOperandsOrderInspector inspector = new ComparisonOperandsOrderInspector();
        ComparisonStyle.force(ComparisonStyle.REGULAR);

        myFixture.configureByFile("fixtures/codeStyle/comparison-order-regular.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        ComparisonStyle.force(ComparisonStyle.YODA);
    }
}
