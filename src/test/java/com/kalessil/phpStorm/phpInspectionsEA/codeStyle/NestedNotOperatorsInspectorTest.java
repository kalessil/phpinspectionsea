package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.NestedNotOperatorsInspector;

final public class NestedNotOperatorsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new NestedNotOperatorsInspector());
        myFixture.configureByFile("fixtures/codeStyle/nested-not-operators.php");
        myFixture.testHighlighting(true, false, true);
    }
}
