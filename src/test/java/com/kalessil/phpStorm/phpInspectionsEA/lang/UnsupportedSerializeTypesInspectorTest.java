package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnsupportedSerializeTypesInspector;

final public class UnsupportedSerializeTypesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnsupportedSerializeTypesInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/unsupported-serialize-types.php");
        myFixture.testHighlighting(true, false, true);
    }
}