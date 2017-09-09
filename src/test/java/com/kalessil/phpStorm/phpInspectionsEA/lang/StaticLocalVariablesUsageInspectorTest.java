package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.StaticLocalVariablesUsageInspector;

final public class StaticLocalVariablesUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StaticLocalVariablesUsageInspector());
        myFixture.configureByFile("fixtures/lang/static-local-variables.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFalsePositives() {
        myFixture.enableInspections(new StaticLocalVariablesUsageInspector());
        myFixture.configureByFile("fixtures/lang/static-local-variables-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}