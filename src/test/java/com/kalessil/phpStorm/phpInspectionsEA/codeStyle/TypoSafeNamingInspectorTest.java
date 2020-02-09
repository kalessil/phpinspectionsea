package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.TypoSafeNamingInspector;

public class TypoSafeNamingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final TypoSafeNamingInspector inspector = new TypoSafeNamingInspector();
        inspector.ALLOW_GETTER_SETTER_PAIRS     = true;
        inspector.ALLOW_SINGULAR_PLURAL_PAIRS   = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/typo-safe-naming.php");
        myFixture.testHighlighting(true, false, true);
    }
}
