package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnnecessaryContinueInspector;

final public class UnnecessaryContinueInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryContinueInspector());
        myFixture.configureByFile("fixtures/controlFlow/unnecessary-continue.php");
        myFixture.testHighlighting(true, false, true);
    }
}
