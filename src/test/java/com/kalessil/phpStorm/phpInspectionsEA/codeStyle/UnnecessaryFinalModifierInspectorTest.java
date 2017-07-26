package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryFinalModifierInspector;

final public class UnnecessaryFinalModifierInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unnecessary-final-modifier.php");
        myFixture.enableInspections(UnnecessaryFinalModifierInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
