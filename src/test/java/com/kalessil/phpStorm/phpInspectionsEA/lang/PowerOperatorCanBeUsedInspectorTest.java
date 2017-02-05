package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.PowerOperatorCanBeUsedInspector;

final public class PowerOperatorCanBeUsedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/power-operator.php");
        myFixture.enableInspections(PowerOperatorCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
