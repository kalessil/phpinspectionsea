package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ExitUsageCorrectnessInspector;

final public class ExitUsageCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ExitUsageCorrectnessInspector());
        myFixture.configureByFile("testData/fixtures/lang/exit-statement.php");
        myFixture.testHighlighting(true, false, true);
    }
}