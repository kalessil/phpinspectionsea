package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnsupportedStringOffsetOperationsInspector;

final public class UnsupportedStringOffsetOperationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnsupportedStringOffsetOperationsInspector());
        myFixture.configureByFile("fixtures/lang/unsupported-string-offset.php");
        myFixture.testHighlighting(true, false, true);
    }
}
