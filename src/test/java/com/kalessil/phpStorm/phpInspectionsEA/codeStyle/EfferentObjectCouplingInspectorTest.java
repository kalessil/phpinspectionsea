package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.EfferentObjectCouplingInspector;

final public class EfferentObjectCouplingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFalsePositives() {
        final EfferentObjectCouplingInspector inspector = new EfferentObjectCouplingInspector();
        inspector.optionCouplingLimit                   = 2;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/efferent-object-coupling.php");
        myFixture.testHighlighting(true, false, true);
    }
}
