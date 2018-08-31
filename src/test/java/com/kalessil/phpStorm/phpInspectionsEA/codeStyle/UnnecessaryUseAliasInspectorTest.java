package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.UnnecessaryUseAliasInspector;

final public class UnnecessaryUseAliasInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnnecessaryUseAliasInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/use-aliases.php");
        myFixture.testHighlighting(true, false, true);
    }
}
