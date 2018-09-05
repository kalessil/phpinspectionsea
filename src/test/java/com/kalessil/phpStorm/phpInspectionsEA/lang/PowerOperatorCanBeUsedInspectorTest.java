package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.PowerOperatorCanBeUsedInspector;

final public class PowerOperatorCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new PowerOperatorCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/lang/power-operator.php");
        myFixture.testHighlighting(true, false, true);
    }
}
