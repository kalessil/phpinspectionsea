package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ComparisonOperandsOrderInspector;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

final public class ComparisonOperandsOrderInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsYodaPatterns() {
        ComparisonStyle.force(ComparisonStyle.YODA);

        myFixture.enableInspections(new ComparisonOperandsOrderInspector());
        myFixture.configureByFile("fixtures/codeStyle/comparison-order-yoda.php");
        myFixture.testHighlighting(true, false, true);

        ComparisonStyle.force(ComparisonStyle.REGULAR);
    }
    public void testIfFindsRegularPatterns() {
        myFixture.enableInspections(new ComparisonOperandsOrderInspector());
        myFixture.configureByFile("fixtures/codeStyle/comparison-order-regular.php");
        myFixture.testHighlighting(true, false, true);
    }
}
