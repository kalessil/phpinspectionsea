package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ShortOpenTagUsageInspector;

final public class ShortOpenTagUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/short-open-tags.php");
        myFixture.enableInspections(ShortOpenTagUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}